import { useCallback, useDeferredValue, useEffect, useMemo, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link, useNavigate } from 'react-router-dom'
import {
  Boxes,
  Camera,
  CircleDollarSign,
  Layers3,
  LayoutGrid,
  LogOut,
  PackagePlus,
  Search,
  Sparkles,
  Store,
  Tags,
  Warehouse,
} from 'lucide-react'
import {
  createCategory,
  createProduct,
  getCategories,
  getProductAggregate,
  getProducts,
  getStock,
  logoutUser,
  updateCategory,
  updateProduct,
  updateStock,
} from '../services/api'
import { clearSession } from '../redux/authSlice'
import { usePrivateAxios } from '../hooks/usePrivateAxios'
import { useAccountData } from '../hooks/useAccountData'
import {
  AppField,
  AppTextarea,
  EmptyPanel,
  SectionCard,
  StatusBanner,
  WorkspacePage,
} from '../components/WorkspaceUI'

const sellerTabs = [
  { key: 'overview', label: 'Resumen', icon: Sparkles },
  { key: 'publish', label: 'Publicar', icon: PackagePlus },
  { key: 'stock', label: 'Inventario', icon: Warehouse },
  { key: 'categories', label: 'Categorias', icon: Tags },
]

const emptyPublishForm = {
  name: '',
  description: '',
  price: '',
  imageMode: 'file', // 'file' | 'url'
  imageFiles: [],
  imageBase64s: [],
  imageUrls: '',
  categoryIds: [],
}

const emptyEditor = {
  productId: '',
  originalName: '',
  originalDescription: '',
  originalPrice: '',
  name: '',
  description: '',
  price: '',
  existingCategories: [],
  additionalCategoryIds: [],
}

function wait(ms) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms)
  })
}

function formatCurrency(value) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 2,
  }).format(Number(value ?? 0))
}

function parseImageUrls(value) {
  return value
    .split(/\r?\n|,/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function extractApiMessage(error) {
  const data = error?.response?.data

  if (typeof data === 'string' && data.trim()) {
    return data
  }

  if (typeof data?.message === 'string' && data.message.trim()) {
    return data.message
  }

  return ''
}

function getCreateProductMessage(error) {
  const responseMessage = extractApiMessage(error)
  const status = Number(error?.response?.status ?? 0)

  if (status === 500) {
    return 'No pudimos publicar este producto por ahora. Guarda el formulario e intentalo de nuevo en unos minutos.'
  }

  if (status === 403) {
    return 'Tu cuenta todavia no esta lista para publicar desde este panel.'
  }

  if (responseMessage) {
    return responseMessage
  }

  return 'No pudimos crear el producto.'
}

function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result) // includes data:image/...;base64, prefix
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

function validatePublishForm(form) {
  const price = Number(form.price)
  const hasImages = form.imageMode === 'file'
    ? form.imageBase64s.length > 0
    : parseImageUrls(form.imageUrls).length > 0

  if (form.name.trim().length < 4) return 'Dale al producto un nombre mas claro.'
  if (form.description.trim().length < 20) return 'Agrega una descripcion un poco mas completa para ayudar a quien compra.'
  if (!Number.isFinite(price) || price <= 0) return 'Define un precio valido para publicar.'
  if (!hasImages) return 'Agrega al menos una imagen del producto.'
  if (form.categoryIds.length === 0) return 'Selecciona al menos una categoria antes de publicar.'

  return null
}

function validateEditor(editor) {
  if (editor.name.trim().length < 4) return 'Dale al producto un nombre mas claro.'
  if (editor.description.trim().length < 20) return 'Agrega una descripcion mas completa para este producto.'
  if (!Number.isFinite(Number(editor.price)) || Number(editor.price) <= 0) return 'Define un precio valido antes de guardar.'
  return null
}

function InventoryBadge({ product }) {
  const isOutOfStock = Number(product.availableStock ?? 0) <= 0

  return (
    <span className={`rounded-full px-3 py-1 text-xs font-semibold ${isOutOfStock ? 'bg-red-50 text-red-700' : 'bg-emerald-50 text-emerald-700'
      }`}>
      {isOutOfStock ? 'Sin stock' : `${product.availableStock} disponibles`}
    </span>
  )
}

function ProductRow({ product, onEdit }) {
  return (
    <article className="rounded-[26px] border border-[#eadfce] bg-white p-5 shadow-sm">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div className="flex items-center gap-4">
          {product.imageUrls?.[0] ? (
            <img src={product.imageUrls[0]} alt={product.name} className="h-20 w-20 rounded-3xl bg-[#f7f1e8] object-contain p-2" />
          ) : (
            <div className="flex h-20 w-20 items-center justify-center rounded-3xl bg-[#f3ede3] text-xs text-[#8b9198]">
              Sin imagen
            </div>
          )}
          <div className="min-w-0">
            <p className="line-clamp-2 text-base font-semibold text-[#1f2328]">{product.name}</p>
            <p className="mt-1 text-sm text-[#6a7077]">{formatCurrency(product.price)}</p>
            <div className="mt-3 flex flex-wrap gap-2">
              {(product.categories ?? []).map((category) => (
                <span key={category} className="rounded-full bg-[#f7efe2] px-3 py-1 text-[11px] font-semibold text-[#b66d19]">
                  {category}
                </span>
              ))}
            </div>
          </div>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <InventoryBadge product={product} />
          <button type="button" onClick={() => onEdit(product.id)} className="rounded-2xl border border-[#ded3c4] px-4 py-3 text-sm font-semibold text-[#4e5358] hover:bg-[#f3ede3]">
            Editar
          </button>
        </div>
      </div>
    </article>
  )
}

function StockRow({ product, client }) {
  const [stock, setStock] = useState(null)
  const [amount, setAmount] = useState(1)
  const [feedback, setFeedback] = useState(null)

  const loadStock = useCallback(async () => {
    try {
      setStock(await getStock(product.id, client))
    } catch {
      setStock(null)
    }
  }, [client, product.id])

  useEffect(() => {
    loadStock().catch(() => { })
  }, [loadStock])

  const handleAdjust = async (type) => {
    try {
      await updateStock(product.id, amount, type, client)
      await loadStock()
      setFeedback({ type: 'success', text: 'Existencias actualizadas.' })
    } catch (error) {
      setFeedback({ type: 'error', text: extractApiMessage(error) || 'No pudimos actualizar las existencias.' })
    }
  }

  return (
    <article className="rounded-[26px] border border-[#eadfce] bg-white p-5 shadow-sm">
      <div className="flex flex-col gap-5 xl:flex-row xl:items-center xl:justify-between">
        <div className="flex items-center gap-4">
          {product.imageUrls?.[0] ? (
            <img src={product.imageUrls[0]} alt={product.name} className="h-20 w-20 rounded-3xl bg-[#f7f1e8] object-contain p-2" />
          ) : (
            <div className="flex h-20 w-20 items-center justify-center rounded-3xl bg-[#f3ede3] text-xs text-[#8b9198]">
              Sin imagen
            </div>
          )}
          <div>
            <p className="font-semibold text-[#1f2328]">{product.name}</p>
            <p className="mt-1 text-sm text-[#6a7077]">Ref. {product.id}</p>
            <div className="mt-3 flex flex-wrap gap-2">
              {(product.categories ?? []).map((category) => (
                <span key={category} className="rounded-full bg-[#f7efe2] px-3 py-1 text-[11px] font-semibold text-[#b66d19]">
                  {category}
                </span>
              ))}
            </div>
          </div>
        </div>
        <div className="flex flex-col gap-3 xl:min-w-[26rem]">
          <div className="flex flex-wrap items-center gap-3 text-sm text-[#5f6368]">
            <span>Total: <strong>{stock?.quantity?.toString() ?? '0'}</strong></span>
            <span>Disponible: <strong>{stock?.availableStock?.toString() ?? String(product.availableStock ?? 0)}</strong></span>
            <InventoryBadge product={{ availableStock: stock?.availableStock ?? product.availableStock }} />
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <label className="flex items-center gap-2 rounded-2xl border border-[#ded3c4] bg-[#fffdf9] px-3 py-2 text-sm text-[#5f6368]">
              <span className="text-xs font-semibold uppercase tracking-[0.2em] text-[#8b9198]">Cantidad</span>
              <input
                type="number"
                min={1}
                value={amount}
                onChange={(event) => setAmount(Math.max(1, Number(event.target.value) || 1))}
                className="w-20 bg-transparent text-right outline-none"
              />
            </label>
            <button type="button" onClick={() => handleAdjust('add')} className="rounded-2xl bg-emerald-500 px-4 py-3 text-sm font-semibold text-white hover:bg-emerald-600">
              Agregar
            </button>
            <button type="button" onClick={() => handleAdjust('reduce')} className="rounded-2xl bg-[#1f2328] px-4 py-3 text-sm font-semibold text-white hover:bg-[#2b3037]">
              Reducir
            </button>
          </div>
          <StatusBanner message={feedback} />
        </div>
      </div>
    </article>
  )
}

export default function Inventory() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const client = usePrivateAxios()
  const { email } = useSelector((state) => state.auth)
  const { company, loading: accountLoading } = useAccountData()
  const [activeTab, setActiveTab] = useState('overview')
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [search, setSearch] = useState('')
  const deferredSearch = useDeferredValue(search)
  const [newCategory, setNewCategory] = useState('')
  const [categoryDrafts, setCategoryDrafts] = useState({})
  const [publishForm, setPublishForm] = useState(emptyPublishForm)
  const [editor, setEditor] = useState(emptyEditor)
  const [loading, setLoading] = useState(true)
  const [feedback, setFeedback] = useState(null)
  const [busy, setBusy] = useState({ categoryCreate: false, categoryUpdateId: null, publish: false, editor: false })
  const companyId = company?.id ?? null

  const loadData = useCallback(async () => {
    if (!companyId) {
      setProducts([])
      setCategoryDrafts({})
      setLoading(false)
      return { ownProducts: [], categories }  // keep existing categories
    }

    setLoading(true)
    try {
      const [productsResponse, categoriesResponse] = await Promise.all([getProducts(), getCategories()])
      const ownProducts = productsResponse
        .filter((product) => Number(product.companyId) === Number(companyId))
        .sort((left, right) => new Date(right.createdAt ?? 0) - new Date(left.createdAt ?? 0))

      setProducts(ownProducts)
      setCategories(categoriesResponse)
      setCategoryDrafts(Object.fromEntries(categoriesResponse.map((category) => [category.id, category.name])))
      return { ownProducts, categories: categoriesResponse }
    } finally {
      setLoading(false)
    }
  }, [companyId])

  // NEW: load categories independently, regardless of companyId
  useEffect(() => {
    getCategories()
      .then((data) => {
        setCategories(data)
        setCategoryDrafts(Object.fromEntries(data.map((c) => [c.id, c.name])))
      })
      .catch(() => { })
  }, [])

  useEffect(() => {
    if (accountLoading) return
    loadData().catch(() => {
      setFeedback({ type: 'error', text: 'No pudimos cargar tu panel en este momento.' })
      setLoading(false)
    })
  }, [accountLoading, loadData])

  const filteredProducts = useMemo(() => {
    const query = deferredSearch.trim().toLowerCase()
    if (!query) return products

    return products.filter((product) => (
      product.name.toLowerCase().includes(query) ||
      String(product.id).includes(query) ||
      (product.categories ?? []).some((category) => category.toLowerCase().includes(query))
    ))
  }, [deferredSearch, products])

  const availableUnits = useMemo(
    () => products.reduce((sum, product) => sum + Number(product.availableStock ?? 0), 0),
    [products],
  )

  const latestProducts = useMemo(() => products.slice(0, 4), [products])

  const metrics = useMemo(() => ([
    { label: 'Tienda', value: company?.name || 'Sin tienda', icon: Store, hint: 'Tu marca y tu operacion se administran desde aqui.' },
    { label: 'Productos', value: String(products.length), icon: LayoutGrid, hint: 'Publicaciones visibles de tu tienda.' },
    { label: 'Unidades listas', value: String(availableUnits), icon: Warehouse, hint: 'Stock disponible para vender hoy.' },
    { label: 'Cuenta', value: email || 'Sin email', icon: Layers3, hint: 'Un acceso para entrar, un panel separado para vender.' },
  ]), [availableUnits, company?.name, email, products.length])

  const refreshPublishedProduct = useCallback(async (expectedName) => {
    const normalizedName = expectedName.trim().toLowerCase()

    for (let attempt = 0; attempt < 4; attempt += 1) {
      const result = await loadData().catch(() => null)
      const appeared = result?.ownProducts?.some((product) => product.name.trim().toLowerCase() === normalizedName)
      if (appeared) return true
      await wait(800 * (attempt + 1))
    }

    return false
  }, [loadData])

  const handleCreateCategory = async (event) => {
    event.preventDefault()
    const trimmedName = newCategory.trim()
    if (trimmedName.length < 2) {
      setFeedback({ type: 'error', text: 'Escribe un nombre mas claro para la categoria.' })
      return
    }

    setBusy((current) => ({ ...current, categoryCreate: true }))
    setFeedback(null)

    try {
      await createCategory({ name: trimmedName }, client)
      setNewCategory('')
      await loadData()
      setFeedback({ type: 'success', text: 'Categoria creada.' })
    } catch (error) {
      setFeedback({ type: 'error', text: extractApiMessage(error) || 'No pudimos crear la categoria.' })
    } finally {
      setBusy((current) => ({ ...current, categoryCreate: false }))
    }
  }

  const handleUpdateCategory = async (id) => {
    const nextName = String(categoryDrafts[id] ?? '').trim()
    if (nextName.length < 2) {
      setFeedback({ type: 'error', text: 'Escribe un nombre mas claro para la categoria.' })
      return
    }

    setBusy((current) => ({ ...current, categoryUpdateId: id }))
    setFeedback(null)

    try {
      await updateCategory(id, { name: nextName }, client)
      await loadData()
      setFeedback({ type: 'success', text: 'Categoria actualizada.' })
    } catch (error) {
      setFeedback({ type: 'error', text: extractApiMessage(error) || 'No pudimos actualizar la categoria.' })
    } finally {
      setBusy((current) => ({ ...current, categoryUpdateId: null }))
    }
  }

  const handleCreateProduct = async (event) => {
    event.preventDefault()
    const validationMessage = validatePublishForm(publishForm)
    if (validationMessage) {
      setFeedback({ type: 'error', text: validationMessage })
      return
    }

    setBusy((current) => ({ ...current, publish: true }))
    setFeedback(null)

    const payload = {
      name: publishForm.name.trim(),
      description: publishForm.description.trim(),
      price: Number(publishForm.price),
      imageUrls: publishForm.imageMode === 'file'
        ? publishForm.imageBase64s
        : parseImageUrls(publishForm.imageUrls),
      categoryIds: publishForm.categoryIds,
    }

    try {
      await createProduct(payload, client)
      setPublishForm(emptyPublishForm)
      const appeared = await refreshPublishedProduct(payload.name)
      setFeedback({
        type: 'success',
        text: appeared
          ? 'Producto publicado y visible en tu panel.'
          : 'Recibimos tu producto. Puede tardar un momento en aparecer en tu vitrina.',
      })
      setActiveTab('overview')
    } catch (error) {
      setFeedback({ type: 'error', text: getCreateProductMessage(error) })
    } finally {
      setBusy((current) => ({ ...current, publish: false }))
    }
  }

  const openEditor = async (productId) => {
    setFeedback(null)
    try {
      const aggregate = await getProductAggregate(productId, client)
      setEditor({
        productId,
        originalName: aggregate.name,
        originalDescription: aggregate.description,
        originalPrice: String(aggregate.price),
        name: aggregate.name,
        description: aggregate.description,
        price: String(aggregate.price),
        existingCategories: aggregate.categories ?? [],
        additionalCategoryIds: [],
      })
      setActiveTab('publish')
    } catch {
      setFeedback({ type: 'error', text: 'No pudimos abrir este producto.' })
    }
  }

  const handleUpdateProduct = async (event) => {
    event.preventDefault()
    const validationMessage = validateEditor(editor)
    if (validationMessage) {
      setFeedback({ type: 'error', text: validationMessage })
      return
    }

    const payload = {}
    if (editor.name.trim() !== editor.originalName) payload.name = editor.name.trim()
    if (editor.description.trim() !== editor.originalDescription) payload.description = editor.description.trim()
    if (editor.price !== editor.originalPrice) payload.price = Number(editor.price)
    if (editor.additionalCategoryIds.length) payload.categoryIds = editor.additionalCategoryIds

    if (!Object.keys(payload).length) {
      setFeedback({ type: 'error', text: 'Aun no hiciste cambios.' })
      return
    }

    setBusy((current) => ({ ...current, editor: true }))
    setFeedback(null)

    try {
      await updateProduct(editor.productId, payload, client)
      setEditor(emptyEditor)
      await loadData()
      setFeedback({ type: 'success', text: 'Producto actualizado.' })
      setActiveTab('overview')
    } catch (error) {
      setFeedback({ type: 'error', text: extractApiMessage(error) || 'No pudimos actualizar el producto.' })
    } finally {
      setBusy((current) => ({ ...current, editor: false }))
    }
  }

  const handleLogout = async () => {
    try {
      await logoutUser()
    } catch {
      // Keep logout resilient even if the cookie is already invalid.
    }

    dispatch(clearSession())
    navigate('/', { replace: true })
  }

  if (accountLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[#f6f3ee]">
        <div className="h-10 w-10 animate-spin rounded-full border-4 border-[#d67d1f] border-t-transparent" />
      </div>
    )
  }

  if (!company) {
    return (
      <WorkspacePage
        eyebrow="Panel vendedor"
        title="Tu espacio de venta se activa cuando la tienda esta lista"
        description="Primero terminamos la configuracion base. Despues aqui administraras publicaciones, categorias e inventario."
      >
        <EmptyPanel
          title="Todavia no encontramos tu tienda"
          description="Completa la configuracion inicial para entrar al panel vendedor y separar por completo la experiencia de venta de la experiencia de compra."
          action={(
            <Link to="/sell" className="inline-flex items-center gap-2 rounded-2xl bg-[#d67d1f] px-4 py-3 text-sm font-semibold text-white hover:bg-[#bd6c16]">
              <Store size={16} />
              Continuar configuracion
            </Link>
          )}
        />
      </WorkspacePage>
    )
  }

  return (
    <WorkspacePage
      eyebrow="Panel vendedor"
      title={company.name}
      description="Aqui concentras la operacion de tu tienda con una vista mas ordenada: resumen, publicaciones, inventario y categorias en pestanas separadas."
      actions={[
        <Link key="catalog" to="/catalog" className="inline-flex items-center gap-2 rounded-2xl border border-[#ded3c4] px-4 py-3 text-sm font-semibold text-[#4e5358] hover:bg-[#f3ede3]">
          <Search size={16} />
          Ver marketplace
        </Link>,
        <button key="logout" type="button" onClick={handleLogout} className="inline-flex items-center gap-2 rounded-2xl bg-[#1f2328] px-4 py-3 text-sm font-semibold text-white hover:bg-[#2b3037]">
          <LogOut size={16} />
          Salir
        </button>,
      ]}
      metrics={metrics}
      tabs={sellerTabs}
      activeTab={activeTab}
      onTabChange={setActiveTab}
    >
      <StatusBanner message={feedback} />

      {activeTab === 'overview' ? (
        <>
          <div className="grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
            <SectionCard title="Panorama rapido" description="Un resumen claro para decidir tu siguiente movimiento sin recorrer todo el panel." icon={Sparkles}>
              <div className="grid gap-4 md:grid-cols-2">
                <div className="rounded-3xl bg-[#f7f1e8] p-5">
                  <p className="text-sm text-[#6a7077]">Ventas preparadas</p>
                  <p className="mt-2 text-2xl font-black tracking-tight text-[#1f2328]">{products.length}</p>
                  <p className="mt-2 text-sm text-[#5f6368]">Tus publicaciones activas listas para seguir creciendo.</p>
                </div>
                <div className="rounded-3xl bg-[#f7f1e8] p-5">
                  <p className="text-sm text-[#6a7077]">Inventario disponible</p>
                  <p className="mt-2 text-2xl font-black tracking-tight text-[#1f2328]">{availableUnits}</p>
                  <p className="mt-2 text-sm text-[#5f6368]">Revisa este numero antes de impulsar cualquier producto.</p>
                </div>
              </div>
              <div className="mt-5 flex flex-wrap gap-3">
                <button type="button" onClick={() => setActiveTab('publish')} className="rounded-2xl bg-[#d67d1f] px-4 py-3 text-sm font-semibold text-white hover:bg-[#bd6c16]">
                  Publicar producto
                </button>
                <button type="button" onClick={() => setActiveTab('stock')} className="rounded-2xl border border-[#ded3c4] px-4 py-3 text-sm font-semibold text-[#4e5358] hover:bg-[#f3ede3]">
                  Revisar inventario
                </button>
                <button type="button" onClick={() => setActiveTab('categories')} className="rounded-2xl border border-[#ded3c4] px-4 py-3 text-sm font-semibold text-[#4e5358] hover:bg-[#f3ede3]">
                  Ordenar categorias
                </button>
              </div>
            </SectionCard>

            <SectionCard title="Productos recientes" description="Los ultimos productos que tienes visibles en tu panel." icon={LayoutGrid}>
              {loading ? (
                <div className="flex justify-center py-12">
                  <div className="h-10 w-10 animate-spin rounded-full border-4 border-[#d67d1f] border-t-transparent" />
                </div>
              ) : latestProducts.length ? (
                <div className="space-y-4">
                  {latestProducts.map((product) => (
                    <ProductRow key={product.id} product={product} onEdit={openEditor} />
                  ))}
                </div>
              ) : (
                <EmptyPanel
                  title="Aun no has publicado productos"
                  description="Empieza con un producto bien descrito y con imagenes claras para que tu vitrina se vea solida desde el inicio."
                  action={(
                    <button type="button" onClick={() => setActiveTab('publish')} className="rounded-2xl bg-[#d67d1f] px-4 py-3 text-sm font-semibold text-white hover:bg-[#bd6c16]">
                      Crear mi primer producto
                    </button>
                  )}
                />
              )}
            </SectionCard>
          </div>

          {!loading && products.length ? (
            <SectionCard title="Valor visible en tu vitrina" description="Una referencia simple para ver cuanto valor tienes hoy en productos publicados." icon={CircleDollarSign}>
              <div className="rounded-[28px] bg-[#f7f1e8] p-6">
                <p className="text-sm text-[#6a7077]">Suma estimada de tus publicaciones</p>
                <p className="mt-2 text-3xl font-black tracking-tight text-[#1f2328]">
                  {formatCurrency(products.reduce((sum, product) => sum + Number(product.price ?? 0), 0))}
                </p>
                <p className="mt-3 text-sm text-[#5f6368]">
                  Usa este dato como una lectura rapida de tu escaparate actual, no como una cuenta contable final.
                </p>
              </div>
            </SectionCard>
          ) : null}
        </>
      ) : null}

      {activeTab === 'publish' ? (
        <div className="grid gap-6 xl:grid-cols-[1.05fr_0.95fr]">
          <SectionCard title="Nueva publicacion" description="Crea fichas mas claras y comerciales." icon={PackagePlus}>
            <form className="space-y-4" onSubmit={handleCreateProduct}>
              <AppField
                label="Nombre"
                value={publishForm.name}
                onChange={(e) => setPublishForm((c) => ({ ...c, name: e.target.value }))}
                placeholder="Ej: Lampara de escritorio minimalista"
              />
              <AppTextarea
                label="Descripcion"
                value={publishForm.description}
                onChange={(e) => setPublishForm((c) => ({ ...c, description: e.target.value }))}
                placeholder="Cuenta por que este producto vale la pena y como puede usarse."
                rows={5}
              />
              <AppField
                label="Precio"
                type="number"
                value={publishForm.price}
                onChange={(e) => setPublishForm((c) => ({ ...c, price: e.target.value }))}
                placeholder="Ej: 59.90"
              />

              {/* Image upload */}
              <div>
                <div className="mb-3 flex items-center justify-between">
                  <p className="text-sm font-medium text-[#30343a]">Imagenes del producto</p>
                  <div className="flex overflow-hidden rounded-xl border border-[#ded3c4] text-xs font-semibold">
                    <button
                      type="button"
                      onClick={() => setPublishForm((c) => ({ ...c, imageMode: 'file' }))}
                      className={`px-3 py-1.5 transition ${publishForm.imageMode === 'file'
                        ? 'bg-[#1f2328] text-white'
                        : 'bg-white text-[#5f6368] hover:bg-[#f3ede3]'
                        }`}
                    >
                      Subir archivo
                    </button>
                    <button
                      type="button"
                      onClick={() => setPublishForm((c) => ({ ...c, imageMode: 'url' }))}
                      className={`px-3 py-1.5 transition ${publishForm.imageMode === 'url'
                        ? 'bg-[#1f2328] text-white'
                        : 'bg-white text-[#5f6368] hover:bg-[#f3ede3]'
                        }`}
                    >
                      Pegar URLs
                    </button>
                  </div>
                </div>

                {publishForm.imageMode === 'file' ? (
                  <>
                    <label className="flex cursor-pointer flex-col items-center gap-2 rounded-2xl border-2 border-dashed border-[#ded3c4] bg-[#fffdf9] px-4 py-6 text-sm text-[#8b9198] transition hover:border-[#d67d1f] hover:bg-[#fff8f0]">
                      <span className="text-2xl"><Camera className='size-10' /></span>
                      <span>Haz clic para subir imágenes</span>
                      <span className="text-xs text-[#aaa]">JPG, PNG, WEBP — puedes seleccionar varias</span>
                      <input
                        type="file"
                        accept="image/*"
                        multiple
                        className="hidden"
                        onChange={async (e) => {
                          const files = Array.from(e.target.files)
                          if (!files.length) return
                          const base64s = await Promise.all(files.map(fileToBase64))
                          setPublishForm((c) => ({
                            ...c,
                            imageFiles: [...c.imageFiles, ...files],
                            imageBase64s: [...c.imageBase64s, ...base64s],
                          }))
                          e.target.value = ''
                        }}
                      />
                    </label>

                    {publishForm.imageFiles.length > 0 && (
                      <div className="mt-3 grid grid-cols-4 gap-2">
                        {publishForm.imageFiles.map((file, index) => (
                          <div key={index} className="relative">
                            <img
                              src={publishForm.imageBase64s[index]}
                              alt={file.name}
                              className="h-20 w-full rounded-2xl object-cover"
                            />
                            <button
                              type="button"
                              onClick={() => setPublishForm((c) => ({
                                ...c,
                                imageFiles: c.imageFiles.filter((_, i) => i !== index),
                                imageBase64s: c.imageBase64s.filter((_, i) => i !== index),
                              }))}
                              className="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-[#1f2328] text-[10px] font-bold text-white hover:bg-red-600"
                            >
                              ✕
                            </button>
                          </div>
                        ))}
                      </div>
                    )}
                  </>
                ) : (
                  <>
                    <AppTextarea
                      label=""
                      value={publishForm.imageUrls}
                      onChange={(e) => setPublishForm((c) => ({ ...c, imageUrls: e.target.value }))}
                      placeholder={'https://ejemplo.com/imagen1.jpg\nhttps://ejemplo.com/imagen2.jpg'}
                      rows={4}
                      hint="Una URL por línea o separadas por coma. Deben empezar por http o https."
                    />
                    {/* URL previews */}
                    {parseImageUrls(publishForm.imageUrls).filter((u) => /^https?:\/\//i.test(u)).length > 0 && (
                      <div className="mt-3 grid grid-cols-4 gap-2">
                        {parseImageUrls(publishForm.imageUrls)
                          .filter((u) => /^https?:\/\//i.test(u))
                          .map((url, index) => (
                            <img
                              key={index}
                              src={url}
                              alt={`Preview ${index + 1}`}
                              className="h-20 w-full rounded-2xl object-cover"
                              onError={(e) => { e.currentTarget.style.display = 'none' }}
                            />
                          ))}
                      </div>
                    )}
                  </>
                )}
              </div>

              {/* Categories — all backend categories, no gate */}
              <div>
                <p className="mb-2 text-sm font-medium text-[#30343a]">Categorias</p>
                {categories.length === 0 ? (
                  <p className="text-sm text-[#8b9198]">Cargando categorias...</p>
                ) : (
                  <div className="grid gap-2 sm:grid-cols-2">
                    {categories.map((category) => (
                      <label
                        key={category.id}
                        className={`flex items-center gap-3 rounded-2xl border px-4 py-3 text-sm transition cursor-pointer ${publishForm.categoryIds.includes(category.id)
                          ? 'border-[#d67d1f] bg-[#fff4e6] text-[#8c5616]'
                          : 'border-[#ded3c4] bg-white text-[#4e5358]'
                          }`}
                      >
                        <input
                          type="checkbox"
                          className="hidden"
                          checked={publishForm.categoryIds.includes(category.id)}
                          onChange={(e) => setPublishForm((c) => ({
                            ...c,
                            categoryIds: e.target.checked
                              ? [...c.categoryIds, category.id]
                              : c.categoryIds.filter((v) => v !== category.id),
                          }))}
                        />
                        <span className={`h-4 w-4 shrink-0 rounded border-2 flex items-center justify-center text-[10px] font-bold ${publishForm.categoryIds.includes(category.id)
                          ? 'border-[#d67d1f] bg-[#d67d1f] text-white'
                          : 'border-[#ded3c4]'
                          }`}>
                          {publishForm.categoryIds.includes(category.id) ? '✓' : ''}
                        </span>
                        {category.name}
                      </label>
                    ))}
                  </div>
                )}
              </div>

              <button
                type="submit"
                disabled={busy.publish}
                className="rounded-2xl bg-[#d67d1f] px-4 py-3 text-sm font-semibold text-white hover:bg-[#bd6c16] disabled:bg-gray-300"
              >
                {busy.publish ? 'Publicando...' : 'Publicar producto'}
              </button>
            </form>
          </SectionCard>

          <SectionCard title="Edicion rapida" description="Ajusta nombre, descripcion, precio o suma nuevas categorias sin salir de tu flujo." icon={LayoutGrid}>
            {editor.productId ? (
              <form className="space-y-4" onSubmit={handleUpdateProduct}>
                <AppField label="Nombre" value={editor.name} onChange={(event) => setEditor((current) => ({ ...current, name: event.target.value }))} />
                <AppTextarea label="Descripcion" value={editor.description} onChange={(event) => setEditor((current) => ({ ...current, description: event.target.value }))} rows={5} />
                <AppField label="Precio" type="number" value={editor.price} onChange={(event) => setEditor((current) => ({ ...current, price: event.target.value }))} />
                <div className="rounded-3xl bg-[#f7f1e8] p-4">
                  <p className="text-sm font-semibold text-[#1f2328]">Categorias actuales</p>
                  <div className="mt-3 flex flex-wrap gap-2">
                    {editor.existingCategories.map((category) => (
                      <span key={category.categoryId} className="rounded-full bg-white px-3 py-1 text-[11px] font-semibold text-[#4e5358]">
                        {category.categoryName}
                      </span>
                    ))}
                  </div>
                </div>
                <div>
                  <p className="mb-2 text-sm font-medium text-[#30343a]">Agregar nuevas categorias</p>
                  <div className="grid gap-2 sm:grid-cols-2">
                    {categories
                      .filter((category) => !editor.existingCategories.some((item) => item.categoryId === category.id))
                      .map((category) => (
                        <label key={category.id} className={`flex items-center gap-3 rounded-2xl border px-4 py-3 text-sm transition ${editor.additionalCategoryIds.includes(category.id)
                          ? 'border-[#d67d1f] bg-[#fff4e6] text-[#8c5616]'
                          : 'border-[#ded3c4] bg-white text-[#4e5358]'
                          }`}>
                          <input
                            type="checkbox"
                            checked={editor.additionalCategoryIds.includes(category.id)}
                            onChange={(event) => setEditor((current) => ({
                              ...current,
                              additionalCategoryIds: event.target.checked
                                ? [...current.additionalCategoryIds, category.id]
                                : current.additionalCategoryIds.filter((value) => value !== category.id),
                            }))}
                          />
                          {category.name}
                        </label>
                      ))}
                  </div>
                </div>
                <div className="flex flex-wrap gap-3">
                  <button type="submit" disabled={busy.editor} className="rounded-2xl bg-[#1f2328] px-4 py-3 text-sm font-semibold text-white hover:bg-[#2b3037] disabled:bg-gray-300">
                    {busy.editor ? 'Guardando...' : 'Guardar cambios'}
                  </button>
                  <button type="button" onClick={() => setEditor(emptyEditor)} className="rounded-2xl border border-[#ded3c4] px-4 py-3 text-sm font-semibold text-[#4e5358] hover:bg-[#f3ede3]">
                    Cerrar editor
                  </button>
                </div>
              </form>
            ) : (
              <EmptyPanel
                title="Selecciona un producto para editar"
                description="Desde Resumen o Inventario puedes abrir cualquier producto y traerlo aqui para ajustar su ficha."
                action={(
                  <button type="button" onClick={() => setActiveTab('overview')} className="rounded-2xl bg-[#1f2328] px-4 py-3 text-sm font-semibold text-white hover:bg-[#2b3037]">
                    Ver mis productos
                  </button>
                )}
              />
            )}
          </SectionCard>
        </div>
      ) : null}

      {activeTab === 'stock' ? (
        <SectionCard
          title="Inventario"
          description="Mantiene existencias bajo control y encuentra rapido el producto que necesites ajustar."
          icon={Warehouse}
          actions={(
            <label className="flex items-center gap-2 rounded-2xl border border-[#ded3c4] bg-white px-4 py-3 text-sm text-[#5f6368]">
              <Search size={16} />
              <input
                type="text"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Buscar por nombre, categoria o referencia"
                className="w-64 max-w-full bg-transparent outline-none"
              />
            </label>
          )}
        >
          {loading ? (
            <div className="flex justify-center py-16">
              <div className="h-10 w-10 animate-spin rounded-full border-4 border-[#d67d1f] border-t-transparent" />
            </div>
          ) : filteredProducts.length ? (
            <div className="space-y-4">
              {filteredProducts.map((product) => (
                <StockRow key={product.id} product={product} client={client} />
              ))}
            </div>
          ) : (
            <EmptyPanel title="No encontramos productos con esa busqueda" description="Prueba con otro nombre o limpia el filtro para revisar todo tu inventario." />
          )}
        </SectionCard>
      ) : null}

      {activeTab === 'categories' ? (
        <div className="relative">
          {/* Blurred content underneath */}
          <div className="pointer-events-none select-none blur-sm">
            <div className="grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
              <SectionCard title="Nueva categoria" description="Crea nombres simples y reconocibles para ordenar mejor tus publicaciones." icon={Tags}>
                <form className="space-y-4">
                  <AppField label="Nombre" value={newCategory} onChange={() => { }} placeholder="Ej: Iluminacion" />
                  <button type="button" disabled className="rounded-2xl bg-[#1f2328] px-4 py-3 text-sm font-semibold text-white disabled:bg-gray-300">
                    Crear categoria
                  </button>
                </form>
              </SectionCard>

              <SectionCard title="Categorias activas" description="Ajusta nombres sin salir del panel y manten un criterio consistente en toda la tienda." icon={Boxes}>
                <div className="space-y-3">
                  {categories.slice(0, 4).map((category) => (
                    <div key={category.id} className="flex flex-col gap-3 rounded-3xl border border-[#eadfce] bg-white p-4 sm:flex-row sm:items-center">
                      <input
                        type="text"
                        readOnly
                        value={categoryDrafts[category.id] ?? category.name}
                        className="w-full rounded-2xl border border-[#ded3c4] px-4 py-3 text-sm outline-none"
                      />
                      <button type="button" disabled className="rounded-2xl border border-[#ded3c4] px-4 py-3 text-sm font-semibold text-[#4e5358] disabled:bg-gray-100">
                        Guardar
                      </button>
                    </div>
                  ))}
                </div>
              </SectionCard>
            </div>
          </div>

          {/* Overlay */}
          <div className="absolute inset-0 flex flex-col items-center justify-center rounded-3xl bg-[#fffdf9]/70 backdrop-blur-[2px]">
            <div className="mx-auto max-w-sm rounded-3xl border border-[#eadfce] bg-white px-8 py-10 text-center shadow-xl">
              <div className="mb-4 flex justify-center">
                <span className="flex h-14 w-14 items-center justify-center rounded-2xl bg-[#f7f1e8]">
                  <Tags size={26} className="text-[#d67d1f]" />
                </span>
              </div>
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[#d67d1f]">Temporalmente no disponible</p>
              <h2 className="mt-3 text-xl font-black tracking-tight text-[#1f2328]">Esta sección está en pausa</h2>
              <p className="mt-3 text-sm leading-6 text-[#6a7077]">
                La gestión de categorías está bajo evaluación. Estamos decidiendo si esta funcionalidad se mantiene, se rediseña, o se elimina completamente del panel.
              </p>
              <p className="mt-4 text-xs text-[#aaa]">Mientras tanto, las categorías existentes siguen activas en el marketplace.</p>
            </div>
          </div>
        </div>
      ) : null}
    </WorkspacePage>
  )
}
