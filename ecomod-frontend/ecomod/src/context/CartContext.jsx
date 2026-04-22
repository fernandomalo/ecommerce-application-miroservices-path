import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { useSelector } from 'react-redux'
import { usePrivateAxios } from '../hooks/usePrivateAxios'
import { DotLottieReact } from '@lottiefiles/dotlottie-react'
import {
  fetchCartByUser,
  fetchCartAnonymous,
  addItemToCart as apiAddItem,
  removeItemFromCart as apiRemoveItem,
  increaseItemQty,
  decreaseItemQty,
  toggleItemStatus as apiToggleStatus,
  checkoutCart as apiCheckout,
} from '../services/api'

const CartContext = createContext(null)

function CartAnimation({ visible }) {
  if (!visible) return null
  return createPortal(
    <div style={{
      position: 'fixed', inset: 0, zIndex: 9999,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      pointerEvents: 'none',
    }}>
      <div style={{
        background: 'rgba(255,255,255,0.92)',
        borderRadius: 24, padding: '12px 24px',
        boxShadow: '0 8px 40px rgba(0,0,0,0.12)',
        display: 'flex', flexDirection: 'column',
        alignItems: 'center', gap: 4,
      }}>
        <DotLottieReact
          src='/add-to-cart-success.lottie'
          autoplay
          style={{ width: 140, height: 140 }}
        />
        <p style={{ fontSize: 13, fontWeight: 600, color: '#1c1917', marginTop: -8 }}>
          ¡Agregado al carrito!
        </p>
      </div>
    </div>,
    document.body
  )
}

export function CartProvider({ children }) {
  const { token } = useSelector((state) => state.auth)

  const [cart, setCart] = useState(null)   // full cart object from backend
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  // Derived helpers
  const items = cart?.items ?? []
  const cartId = cart?.id ?? null
  const total = cart?.totalPrice ?? 0
  const totalShipping = cart?.totalShippingPrice ?? 0
  const totalCharge = cart?.totalCharge ?? 0
  const privateApi = usePrivateAxios()

  const loadCart = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = token
        ? await fetchCartByUser(privateApi)
        : await fetchCartAnonymous()
        console.log('TOKEN:', token)
        console.log('Fetching cart as:', token ? 'AUTH' : 'ANONYMOUS')
      setCart(data)
    } catch {
      // Anonymous user with no cart yet — perfectly fine
      setCart(null)
    } finally {
      setLoading(false)
    }
  }, [token])

  useEffect(() => {
    loadCart()
  }, [loadCart])

  const addItem = useCallback(async (product, ruleId) => {
    await apiAddItem(product.id, ruleId)
    await loadCart()
    setAnimating(true)
    setTimeout(() => setAnimating(false), 2000)
  }, [loadCart])

  const removeItem = useCallback(async (productId) => {
    if (!cartId) return
    await apiRemoveItem(cartId, productId)
    await loadCart()
  }, [cartId, loadCart])

  const increaseQty = useCallback(async (productId) => {
    if (!cartId) return
    await increaseItemQty(cartId, productId)
    await loadCart()
  }, [cartId, loadCart])

  const decreaseQty = useCallback(async (productId) => {
    if (!cartId) return
    await decreaseItemQty(cartId, productId)
    await loadCart()
  }, [cartId, loadCart])

  const toggleStatus = useCallback(async (productId) => {
    if (!cartId) return
    await apiToggleStatus(cartId, productId)
    await loadCart()
  }, [cartId, loadCart])

  const checkout = useCallback(async () => {
    await apiCheckout()
    setCart(null)
  }, [])

  return (
    <CartContext.Provider value={{
      cart,
      items,
      cartId,
      total,
      totalShipping,
      totalCharge,
      loading,
      error,
      loadCart,
      addItem,
      removeItem,
      increaseQty,
      decreaseQty,
      toggleStatus,
      checkout,
    }}>
      {children}
      <CartAnimation visible={animating} />
    </CartContext.Provider>
  )
}

export const useCart = () => useContext(CartContext)