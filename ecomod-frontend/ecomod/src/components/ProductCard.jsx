import { useNavigate } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { isBusinessRole } from '../utils/authRole'

function formatCurrency(value) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 2,
  }).format(Number(value ?? 0))
}

export default function ProductCard({ product }) {
  const navigate = useNavigate()
  const { roles } = useSelector((state) => state.auth)
  const isBusiness = isBusinessRole(roles)
  const firstImage = product.imageUrls?.[0]
  const isOutOfStock = Number(product.availableStock ?? 0) <= 0
  const firstCategory = product.categories?.[0]

  return (
    <article className="group relative flex h-full flex-col overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-lg">
      
      {/* Image area */}
      <div className="relative flex h-52 items-center justify-center bg-gray-50 p-6">
        {firstImage ? (
          <img src={firstImage} alt={product.name} className="max-h-full object-contain" />
        ) : (
          <div className="text-sm text-gray-400">Sin imagen</div>
        )}

        {/* Hover overlay — only for buyers */}
        {!isBusiness && (
          <div className="absolute inset-0 flex flex-col items-center justify-center gap-3 bg-black/50 opacity-0 transition-opacity duration-200 group-hover:opacity-100">
            <button
              type="button"
              onClick={() => navigate(`/product/${product.id}`)}
              className="w-40 rounded-full bg-white py-2 text-sm font-semibold text-[#1f2328] transition hover:bg-gray-100"
            >
              Visualizar
            </button>
            <button
              type="button"
              onClick={() =>
                firstCategory
                  ? navigate(`/catalog?category=${encodeURIComponent(firstCategory)}`)
                  : navigate('/catalog')
              }
              className="w-40 rounded-full border border-white/60 py-2 text-sm font-semibold text-white transition hover:bg-white/10"
            >
              Mirar similares
            </button>
          </div>
        )}
      </div>

      {/* Info area */}
      <div className="flex flex-1 flex-col gap-3 p-4">
        <div className="space-y-2">
          <p className="line-clamp-2 text-sm font-medium text-gray-800">{product.name}</p>
          <div className="flex flex-wrap gap-2">
            {(product.categories ?? []).slice(0, 2).map((category) => (
              <span key={category} className="rounded-full bg-orange-50 px-2 py-1 text-[11px] font-medium text-orange-700">
                {category}
              </span>
            ))}
          </div>
          {product.companyName ? (
            <p className="text-xs text-gray-500">Vendido por {product.companyName}</p>
          ) : null}
        </div>

        <div className="mt-auto">
          <div className="flex items-center justify-between">
            <span className="text-lg font-bold text-orange-500">{formatCurrency(product.price)}</span>
            <span className={`text-xs font-semibold ${isOutOfStock ? 'text-red-500' : 'text-emerald-600'}`}>
              {isOutOfStock ? 'Sin stock' : `${product.availableStock} disponibles`}
            </span>
          </div>
          {/* Business users still get a plain detail link */}
          {isBusiness && (
            <button
              type="button"
              onClick={() => navigate(`/product/${product.id}`)}
              className="mt-3 block w-full rounded-xl border border-gray-300 py-2.5 text-center text-sm font-semibold text-gray-700 transition hover:bg-gray-100"
            >
              Ver detalle
            </button>
          )}
        </div>
      </div>
    </article>
  )
}