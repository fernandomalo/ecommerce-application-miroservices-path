import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { getCompanyBySlug, getProduct, getShippingRule } from '../services/api'
import { useCart } from '../context/CartContext'
import { usePrivateAxios } from '../hooks/usePrivateAxios'
import { isBusinessRole } from '../utils/authRole'

function formatCurrency(value) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 2,
  }).format(Number(value ?? 0))
}

export default function ProductDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { addItem } = useCart()
  const { token, roles } = useSelector((state) => state.auth)
  const isBusiness = isBusinessRole(roles)
  const privateApi = usePrivateAxios()

  const [product, setProduct] = useState(null)
  const [company, setCompany] = useState(null)
  const [shippingRule, setShippingRule] = useState(null)
  const [shippingRuleId, setShippingRuleId] = useState(null) // ← use this when adding to cart
  const [loading, setLoading] = useState(true)
  const [activeImage, setActiveImage] = useState(0)

  useEffect(() => {
    setActiveImage(0)
  }, [id])

  useEffect(() => {
    let ignore = false
    setLoading(true)
    setShippingRule(null)
    setShippingRuleId(null)

    getProduct(id)
      .then(async (productResponse) => {
        console.log("PRODUCT RESPONSE:", productResponse)
        if (ignore) return

        setProduct(productResponse)

        if (token && productResponse.companySlug) {
          try {
            const companyResponse = await getCompanyBySlug(productResponse.companySlug, privateApi)
            if (!ignore) {
              setCompany(companyResponse)
            }
          } catch {
            if (!ignore) {
              setCompany(null)
            }
          }
        }

        // Fetch shipping rule if user is logged in and product has a companyId
        if (token && productResponse.companyId) {
          try {
            if (token && productResponse.companyId) {
              const shippingResponse = await getShippingRule(productResponse.companyId, privateApi)


              if (!ignore) {
                setShippingRule(shippingResponse)
                setShippingRuleId(shippingResponse?.id ?? null)
              }
            }
          } catch {
            if (!ignore) {
              setShippingRule(null)
              setShippingRuleId(null)
            }
          }
        }
      })
      .finally(() => {
        if (!ignore) {
          setLoading(false)
        }
      })

    return () => {
      ignore = true
    }
  }, [id, privateApi, token])

  if (loading) {
    return (
      <div className="flex justify-center py-24">
        <div className="h-10 w-10 animate-spin rounded-full border-4 border-orange-500 border-t-transparent" />
      </div>
    )
  }

  if (!product) {
    return <p className="py-24 text-center text-gray-500">Producto no encontrado.</p>
  }

  const isOutOfStock = Number(product.availableStock ?? 0) <= 0

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-6xl px-4 py-10">
        <button type="button" onClick={() => navigate(-1)} className="mb-6 text-sm font-semibold text-orange-500 hover:underline">
          Volver
        </button>

        <div className="grid gap-8 rounded-3xl border border-gray-200 bg-white p-8 shadow-sm lg:grid-cols-[0.95fr_1.05fr]">
          <div className="flex gap-3">
            {product.imageUrls?.length > 1 && (
              <div className="flex flex-col gap-2">
                {product.imageUrls.map((url, index) => (
                  <button
                    key={url}
                    type="button"
                    onClick={() => setActiveImage(index)}
                    className={`h-20 w-16 shrink-0 overflow-hidden rounded-2xl border-2 transition ${activeImage === index
                      ? 'border-orange-500'
                      : 'border-gray-200 hover:border-gray-300'
                      }`}
                  >
                    <img
                      src={url}
                      alt={`${product.name} ${index + 1}`}
                      className="h-full w-full object-contain p-1"
                    />
                  </button>
                ))}
              </div>
            )}

            <div className="flex flex-1 items-center justify-center rounded-3xl bg-gray-50 p-6">
              {product.imageUrls?.length ? (
                <img
                  key={product.imageUrls[activeImage]}
                  src={product.imageUrls[activeImage]}
                  alt={product.name}
                  className="max-h-[26rem] w-full rounded-2xl object-contain"
                />
              ) : (
                <div className="flex h-full min-h-80 w-full items-center justify-center rounded-2xl border border-dashed border-gray-300 text-sm text-gray-400">
                  Sin imagenes
                </div>
              )}
            </div>
          </div>

          <div className="flex flex-col gap-5">
            <div className="flex flex-wrap gap-2">
              {(product.categories ?? []).map((category) => (
                <span key={category} className="rounded-full bg-orange-50 px-3 py-1 text-xs font-semibold text-orange-700">
                  {category}
                </span>
              ))}
            </div>

            <div>
              <h1 className="text-3xl font-black text-gray-900">{product.name}</h1>
              <p className="mt-2 text-sm text-gray-500">
                {product.companyName ? `Vendido por ${product.companyName}` : 'Producto publicado en EcoMod'}
              </p>
            </div>

            <p className="text-3xl font-bold text-orange-500">{formatCurrency(product.price)}</p>
            <p className="text-base leading-7 text-gray-600">{product.description}</p>

            <div className="grid gap-3 md:grid-cols-2">
              <div className="rounded-2xl bg-gray-50 px-4 py-4">
                <p className="text-xs uppercase tracking-wider text-gray-400">Stock disponible</p>
                <p className={`mt-2 text-lg font-bold ${isOutOfStock ? 'text-red-500' : 'text-emerald-600'}`}>
                  {isOutOfStock ? 'Sin stock' : `${product.availableStock} unidades`}
                </p>
              </div>
              <div className="rounded-2xl bg-gray-50 px-4 py-4">
                <p className="text-xs uppercase tracking-wider text-gray-400">Tienda</p>
                <p className="mt-2 text-sm font-semibold text-gray-900">{product.companyName || 'Aun sin tienda'}</p>
                <p className="text-xs text-gray-500">{isOutOfStock ? 'Vuelve pronto' : 'Disponible para tu pedido'}</p>
              </div>
            </div>

            {/* ── Shipping info ── */}
            {shippingRule ? (
              <div className="rounded-2xl border border-gray-200 px-4 py-4 text-sm">
                <p className="font-semibold text-gray-900 mb-3">Información de envío</p>
                <div className="flex items-start gap-2 mb-3">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mt-0.5 shrink-0 text-gray-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" /><circle cx="12" cy="10" r="3" />
                  </svg>
                  <div>
                    <p className="text-xs text-gray-400">Desde / Hasta</p>
                    <p className="mt-0.5 font-medium text-gray-900">
                      {shippingRule.originZone} → {shippingRule.destinationZone}
                    </p>
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-3 border-t border-gray-100 pt-3">
                  <div>
                    <p className="text-xs text-gray-400">Costo de envío</p>
                    <p className="mt-1 text-base font-bold text-orange-500">{formatCurrency(shippingRule.price)}</p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-400">Tiempo estimado</p>
                    <p className="mt-1 text-base font-bold text-gray-900">
                      {shippingRule.durationTime} {shippingRule.durationTime === 1 ? 'día' : 'días'}
                    </p>
                  </div>
                </div>
              </div>
            ) : token && product.companyId ? (
              <div className="rounded-2xl border border-dashed border-gray-200 px-4 py-4 text-sm text-gray-500">
                No hay información de envío disponible para tu zona.
              </div>
            ) : null}
            {/* ── end shipping info ── */}

            {company ? (
              <div className="rounded-2xl border border-gray-200 px-4 py-4 text-sm text-gray-600">
                <p className="font-semibold text-gray-900">Sobre la tienda</p>
                <p className="mt-2 font-medium text-gray-900">{company.name}</p>
                <p>{company.country}, {company.region}, {company.city}</p>
                <p>{company.location}</p>
              </div>
            ) : token && product.companySlug ? (
              <div className="rounded-2xl border border-dashed border-gray-200 px-4 py-4 text-sm text-gray-500">
                No pudimos cargar mas detalles de la tienda.
              </div>
            ) : null}

            <div className="flex flex-col gap-3 sm:flex-row">
              {isBusiness ? (
                <>
                  <Link
                    to="/seller"
                    className="flex-1 rounded-2xl bg-gray-900 px-4 py-3 text-center text-sm font-semibold text-white transition hover:bg-gray-800"
                  >
                    Ir a mi panel
                  </Link>
                  <Link
                    to="/catalog"
                    className="flex-1 rounded-2xl border border-gray-300 px-4 py-3 text-center text-sm font-semibold text-gray-700 transition hover:bg-gray-100"
                  >
                    Seguir explorando
                  </Link>
                </>
              ) : (
                <>
                  <button
                    type="button"
                    disabled={isOutOfStock}
                    onClick={async () => {
                      await addItem(product, shippingRuleId)
                      navigate('/cart')
                    }}
                    className="flex-1 rounded-2xl bg-orange-500 px-4 py-3 text-sm font-semibold text-white transition hover:bg-orange-600 disabled:cursor-not-allowed disabled:bg-gray-300"
                  >
                    {isOutOfStock ? 'Producto agotado' : 'Comprar ahora'}
                  </button>
                  <button
                    type="button"
                    disabled={isOutOfStock}
                    onClick={() => addItem(product, shippingRuleId)}
                    className="flex-1 rounded-2xl border border-orange-500 px-4 py-3 text-sm font-semibold text-orange-600 transition hover:bg-orange-50 disabled:cursor-not-allowed disabled:border-gray-200 disabled:text-gray-400"
                  >
                    Agregar al carrito
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}