import { Link } from 'react-router-dom'
import { useSelector } from 'react-redux'
import ProductCard from '../components/ProductCard'
import { useProducts } from '../hooks/useProducts'
import { isBusinessRole } from '../utils/authRole'

function formatCurrency(value) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 2,
  }).format(Number(value ?? 0))
}

export default function Home() {
  const { products, loading } = useProducts()
  const { token, roles } = useSelector((state) => state.auth)
  const isBusiness = isBusinessRole(roles)
  const heroProducts = products.slice(0, 3)
  const deals = products.slice(0, 4)
  const featured = products.slice(4, 12)

  return (
    <div className="min-h-screen bg-gray-50">
      <section className="border-b border-orange-100 bg-gradient-to-br from-amber-50 via-white to-orange-100">
        <div className="mx-auto grid max-w-7xl gap-8 px-4 py-12 lg:grid-cols-[1.2fr_0.8fr] lg:items-center">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.25em] text-orange-500">Seleccion EcoMod</p>
            <h1 className="mt-4 text-4xl font-black tracking-tight text-gray-900">
              Encuentra piezas con estilo para cada rincon.
            </h1>
            <p className="mt-4 max-w-2xl text-base text-gray-600">
              Descubre productos listos para inspirar tu espacio, compra con calma y guarda tus favoritos para volver cuando quieras.
            </p>
            <div className="mt-6 flex flex-wrap gap-3">
              <Link to="/catalog" className="rounded-2xl bg-gray-900 px-5 py-3 text-sm font-semibold text-white transition hover:bg-gray-800">
                Ver catalogo
              </Link>
              <Link
                to={token ? (isBusiness ? '/seller' : '/me') : '/account'}
                className="rounded-2xl border border-gray-300 px-5 py-3 text-sm font-semibold text-gray-700 transition hover:bg-white"
              >
                {token ? (isBusiness ? 'Ir a mi panel' : 'Mi cuenta') : 'Entrar o crear cuenta'}
              </Link>
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-3 lg:grid-cols-1">
            {loading ? (
              <div className="rounded-3xl border border-gray-200 bg-white p-8 text-center shadow-sm">
                <div className="mx-auto h-10 w-10 animate-spin rounded-full border-4 border-orange-500 border-t-transparent" />
                <p className="mt-3 text-sm text-gray-500">Preparando destacados para ti...</p>
              </div>
            ) : (
              heroProducts.map((product) => (
                <div key={product.id} className="rounded-3xl border border-white/60 bg-white/80 p-4 shadow-sm">
                  <div className="flex items-center gap-4">
                    {product.imageUrls?.[0] ? (
                      <img src={product.imageUrls[0]} alt={product.name} className="h-20 w-20 rounded-2xl bg-gray-50 object-contain p-2" />
                    ) : (
                      <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-gray-100 text-xs text-gray-400">
                        Sin imagen
                      </div>
                    )}
                    <div className="min-w-0">
                      <p className="line-clamp-2 text-sm font-semibold text-gray-900">{product.name}</p>
                      <p className="mt-1 text-xs text-gray-500">{product.companyName || 'EcoMod'}</p>
                      <p className="mt-2 text-lg font-bold text-orange-500">{formatCurrency(product.price)}</p>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </section>

      <section className="border-b border-gray-200 bg-white">
        <div className="mx-auto flex max-w-7xl flex-wrap justify-center gap-8 px-4 py-4 text-sm text-gray-600">
          <span>Piezas con identidad para tu espacio</span>
          <span>Compra con una cuenta simple y rapida</span>
          <span>Tiendas listas para compartir sus mejores productos</span>
        </div>
      </section>

      <div className="mx-auto flex max-w-7xl flex-col gap-10 px-4 py-10">
        <section>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-2xl font-bold text-gray-900">Ofertas del momento</h2>
            <Link to="/catalog" className="text-sm font-semibold text-orange-500 hover:underline">
              Ver todo
            </Link>
          </div>
          {loading ? (
            <div className="flex justify-center py-10">
              <div className="h-10 w-10 animate-spin rounded-full border-4 border-orange-500 border-t-transparent" />
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              {deals.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}
        </section>

        <section>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-2xl font-bold text-gray-900">Nuevos favoritos</h2>
            <Link to="/catalog" className="text-sm font-semibold text-orange-500 hover:underline">
              Explorar catalogo
            </Link>
          </div>
          {loading ? (
            <div className="flex justify-center py-10">
              <div className="h-10 w-10 animate-spin rounded-full border-4 border-orange-500 border-t-transparent" />
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4">
              {featured.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}
        </section>
      </div>
    </div>
  )
}
