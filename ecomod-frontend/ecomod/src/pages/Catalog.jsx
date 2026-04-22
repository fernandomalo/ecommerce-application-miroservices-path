import { useDeferredValue, useMemo } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import ProductCard from '../components/ProductCard'
import { useProducts } from '../hooks/useProducts'

export default function Catalog() {
  const { products, loading, error } = useProducts()
  const [searchParams] = useSearchParams()
  const search = useDeferredValue((searchParams.get('q') || '').trim().toLowerCase())
  const category = useDeferredValue((searchParams.get('category') || '').trim().toLowerCase())

  const filteredProducts = useMemo(() => {
    return products.filter((product) => {
      const matchesSearch = !search || [
        product.name,
        product.description,
        product.companyName,
        ...(product.categories ?? []),
      ]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(search))

      const matchesCategory = !category || (product.categories ?? [])
        .some((value) => value.toLowerCase() === category)

      return matchesSearch && matchesCategory
    })
  }, [category, products, search])

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-7xl px-4 py-8">
        <div className="mb-6 flex flex-col gap-3 rounded-3xl border border-gray-200 bg-white p-6 shadow-sm">
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-orange-500">Explorar</p>
          <h1 className="text-3xl font-bold text-gray-900">Encuentra lo que va con tu espacio</h1>
          <div className="flex flex-wrap gap-2 text-sm text-gray-500">
            {search ? <span className="rounded-full bg-orange-50 px-3 py-1 text-orange-700">Buscando: {search}</span> : null}
            {category ? <span className="rounded-full bg-gray-100 px-3 py-1 text-gray-700">Categoria: {category}</span> : null}
            {(search || category) ? (
              <Link to="/catalog" className="rounded-full border border-gray-300 px-3 py-1 font-medium text-gray-700 hover:bg-gray-100">
                Limpiar filtros
              </Link>
            ) : null}
          </div>
        </div>

        {loading ? (
          <div className="flex justify-center py-20">
            <div className="h-10 w-10 animate-spin rounded-full border-4 border-orange-500 border-t-transparent" />
          </div>
        ) : null}

        {error ? (
          <p className="rounded-3xl bg-red-50 px-4 py-10 text-center text-red-600">
            Ahora mismo no pudimos cargar esta coleccion.
          </p>
        ) : null}

        {!loading && !error ? (
          filteredProducts.length ? (
            <div className="grid gap-4 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4">
              {filteredProducts.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          ) : (
            <div className="rounded-3xl border border-dashed border-gray-300 bg-white px-6 py-16 text-center text-sm text-gray-500">
              No encontramos productos con esos filtros.
            </div>
          )
        ) : null}
      </div>
    </div>
  )
}
