import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { Minus, Plus, ShoppingCart, Store, Trash2, Truck } from 'lucide-react'
import { useCart } from '../context/CartContext'
import { isBusinessRole } from '../utils/authRole'
import { useState } from 'react'
import {
  EmptyPanel,
  SectionCard,
  WorkspacePage,
} from '../components/WorkspaceUI'

function formatCurrency(value) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    minimumFractionDigits: 2,
  }).format(Number(value ?? 0))
}

export default function Cart() {
  const { items, cartId, total, totalCharge, totalShipping, loading, removeItem, increaseQty, decreaseQty, toggleStatus, checkout } = useCart()
  const { roles } = useSelector((state) => state.auth)
  const isBusiness = isBusinessRole(roles)
  const [activeTab, setActiveTab] = useState('items')
  const [checkoutLoading, setCheckoutLoading] = useState(false)
  const [checkoutError, setCheckoutError] = useState(null)

  if (isBusiness) return <Navigate to="/seller" replace />

  const navigate = useNavigate();

  if (loading) {
    return (
      <div className="flex justify-center py-24">
        <div className="h-10 w-10 animate-spin rounded-full border-4 border-orange-500 border-t-transparent" />
      </div>
    )
  }

  if (!items.length) {
    return (
      <WorkspacePage
        eyebrow="Tu carrito"
        title="Todavia no agregaste productos"
        description="Explora el marketplace y guarda lo que te guste."
      >
        <EmptyPanel
          title="Tu carrito esta vacio"
          description="Cuando agregues productos, aqui veras tu resumen de compra."
          action={(
            <Link to="/catalog" className="inline-flex items-center gap-2 rounded-2xl bg-[#d67d1f] px-4 py-3 text-sm font-semibold text-white hover:bg-[#bd6c16]">
              <Store size={16} />
              Ir al catalogo
            </Link>
          )}
        />
      </WorkspacePage>
    )
  }

  const grandTotal = totalCharge

  const handleCheckout = async () => {
    setCheckoutLoading(true)
    setCheckoutError(null)
    try {
      await checkout()
      navigate('/payment')
    } catch (err) {
      setCheckoutError('No se pudo procesar tu orden. Intenta de nuevo.')
    } finally {
      setCheckoutLoading(false)
    }
  }

  const checkedItems = items.filter(item => item.status === 'CHECKED')

  const previewItems = checkedItems.slice(0, 5)
  const remainingCount = checkedItems.length - previewItems.length

  return (
    <div className="grid mt-4 gap-6 lg:grid-cols-[1.15fr_0.85fr]">
      {/* Items list */}
      <SectionCard title="Productos elegidos" description="Marca los que quieres incluir en tu orden." icon={ShoppingCart}>
        <div className="space-y-4">
          {items.map((item) => {
            const productId = item.product?.productId
            const isChecked = item.status === 'CHECKED'
            const imageUrl = item.product?.images?.[0]

            return (
              <article key={productId} className={`rounded-[26px] border bg-white p-5 shadow-sm transition ${isChecked ? 'border-orange-400' : 'border-[#eadfce]'}`}>
                <div className="flex flex-col gap-4 md:flex-row md:items-center">
                  {/* Checkbox toggle */}
                  <button
                    type="button"
                    onClick={() => toggleStatus(productId)}
                    className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full border-2 transition ${isChecked ? 'border-orange-500 bg-orange-500' : 'border-gray-300'}`}
                  >
                    {isChecked && (
                      <svg width="10" height="10" viewBox="0 0 10 10" fill="none">
                        <path d="M2 5l2.5 2.5L8 3" stroke="white" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    )}
                  </button>

                  {imageUrl ? (
                    <img src={imageUrl} alt={item.product?.name} className="h-24 w-24 rounded-3xl bg-[#f7f1e8] object-contain p-2" />
                  ) : (
                    <div className="flex h-24 w-24 items-center justify-center rounded-3xl bg-[#f3ede3] text-xs text-[#8b9198]">Sin imagen</div>
                  )}

                  <div className="min-w-0 flex-1">
                    <p className="line-clamp-2 text-base font-semibold text-[#1f2328]">{item.product?.name}</p>
                    <p className="mt-1 text-lg font-black text-[#d67d1f]">{formatCurrency(item.product?.price)}</p>
                    <p className="mt-1 text-xs text-gray-500">Subtotal: {formatCurrency(item.subtotal)}</p>
                  </div>

                  <div className="flex flex-col gap-4 md:items-end">
                    <div className="flex items-center gap-3">
                      <button type="button" onClick={() => decreaseQty(productId)} className="flex h-10 w-10 items-center justify-center rounded-full border border-[#ded3c4] text-[#4e5358] hover:bg-[#f3ede3]">
                        <Minus size={16} />
                      </button>
                      <span className="w-8 text-center text-sm font-semibold text-[#1f2328]">{item.quantity?.toString()}</span>
                      <button type="button" onClick={() => increaseQty(productId)} className="flex h-10 w-10 items-center justify-center rounded-full border border-[#ded3c4] text-[#4e5358] hover:bg-[#f3ede3]">
                        <Plus size={16} />
                      </button>
                    </div>
                    <button type="button" onClick={() => removeItem(productId)} className="inline-flex items-center gap-2 text-sm font-semibold text-red-600 hover:underline">
                      <Trash2 size={15} />
                      Quitar
                    </button>
                  </div>
                </div>
              </article>
            )
          })}
        </div>
      </SectionCard>

      {/* Summary */}
      <SectionCard title="Resumen" description="Solo los productos marcados se incluyen en tu orden." icon={ShoppingCart}>
        <div className="space-y-3 text-sm text-[#5f6368]">
          {checkedItems.length > 0 && (
            <div className="mb-4">
              <p className="mb-2 text-xs font-semibold text-[#8b9198] uppercase tracking-wide">
                Productos seleccionados
              </p>

              <div className="flex items-center">
                <div className="flex -space-x-3">
                  {previewItems.map((item, index) => {
                    const imageUrl = item.product?.images?.[0]

                    return (
                      <div
                        key={item.product?.productId}
                        className="h-12 w-12 overflow-hidden rounded-xl border-2 border-white bg-[#f3ede3] shadow-sm"
                        style={{ zIndex: previewItems.length - index }}
                      >
                        {imageUrl ? (
                          <img
                            src={imageUrl}
                            alt={item.product?.name}
                            className="h-full w-full object-contain p-1"
                          />
                        ) : (
                          <div className="flex h-full w-full items-center justify-center text-[10px] text-gray-400">
                            N/A
                          </div>
                        )}
                      </div>
                    )
                  })}
                </div>

                {remainingCount > 0 && (
                  <div className="ml-3 flex h-10 items-center justify-center rounded-xl bg-[#f3ede3] px-3 text-xs font-semibold text-[#4e5358]">
                    +{remainingCount}
                  </div>
                )}
              </div>
            </div>
          )}
          <div className="flex items-center justify-between rounded-2xl bg-[#f7f1e8] px-4 py-3">
            <span>Subtotal productos</span>
            <strong className="text-[#1f2328]">{formatCurrency(total)}</strong>
          </div>
          <div className="flex items-center justify-between rounded-2xl bg-[#f7f1e8] px-4 py-3">
            <span>Envío</span>
            <strong className="text-[#1f2328]">{formatCurrency(totalShipping)}</strong>
          </div>
          <div className="flex items-center justify-between rounded-2xl bg-[#1f2328] px-4 py-4 text-white">
            <span>Total</span>
            <strong>{formatCurrency(grandTotal)}</strong>
          </div>
        </div>

        {checkoutError && (
          <p className="mt-3 text-sm text-red-600">{checkoutError}</p>
        )}

        <div className="mt-5">
          <button
            type="button"
            onClick={handleCheckout}
            disabled={checkoutLoading}
            className="w-full rounded-2xl bg-[#d67d1f] px-4 py-3 text-sm font-semibold text-white hover:bg-[#bd6c16] disabled:bg-gray-300"
          >
            {checkoutLoading ? 'Procesando...' : 'Proceder al pago'}
          </button>
        </div>

        <div className="mt-3 flex items-start gap-2 rounded-2xl bg-[#f7f1e8] px-4 py-3 text-xs text-[#7a6a55]">
          <Truck size={14} className="mt-0.5 shrink-0" />
          <span>El costo de envío se calcula automáticamente por tienda según tu zona.</span>
        </div>
      </SectionCard>
    </div>
  )
}