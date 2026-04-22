import { useEffect, useState } from 'react'
import { useSearchParams, useNavigate, Link } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { usePrivateAxios } from '../hooks/usePrivateAxios'
import {
  Package, ChevronRight, CheckCircle, XCircle,
  Clock, Loader2, MapPin, Truck, ShoppingBag,
  CreditCard, AlertTriangle, ArrowLeft,
} from 'lucide-react'
import {
  getOrdersByUser, getOrderById,
  getPaymentByReference, getShippingRuleById,
} from '../services/api'

// ─── Helpers ────────────────────────────────────────────────────────────────

function formatCOP(value) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency', currency: 'COP', minimumFractionDigits: 0,
  }).format(Number(value ?? 0))
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  return new Intl.DateTimeFormat('es-CO', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit',
  }).format(new Date(dateStr))
}

const STATUS_META = {
  PENDING:    { label: 'Pendiente',   color: '#92610a', bg: '#fef9ee', border: '#f5d98b', dot: '#f0b429' },
  CONFIRMED:  { label: 'Confirmado',  color: '#166534', bg: '#f0fdf4', border: '#86efac', dot: '#22c55e' },
  FAILED:     { label: 'Fallido',     color: '#991b1b', bg: '#fef2f2', border: '#fca5a5', dot: '#ef4444' },
  PROCESSING: { label: 'En proceso',  color: '#1e3a8a', bg: '#eff6ff', border: '#93c5fd', dot: '#3b82f6' },
  APPROVED:   { label: 'Aprobado',    color: '#166534', bg: '#f0fdf4', border: '#86efac', dot: '#22c55e' },
}

function StatusPill({ status }) {
  const m = STATUS_META[status] ?? STATUS_META.PENDING
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: 6,
      background: m.bg, color: m.color,
      border: `1px solid ${m.border}`,
      borderRadius: 99, padding: '3px 10px',
      fontSize: 11, fontWeight: 600, letterSpacing: '0.02em',
    }}>
      <span style={{ width: 6, height: 6, borderRadius: '50%', background: m.dot, flexShrink: 0 }} />
      {m.label}
    </span>
  )
}

// ─── Shipping detail ─────────────────────────────────────────────────────────

function ShippingDetail({ ruleId }) {
  const [rule, setRule] = useState(null)
  const [loading, setLoading] = useState(true)
  const privateApi = usePrivateAxios()

  useEffect(() => {
    if (!ruleId) { setLoading(false); return }
    getShippingRuleById(ruleId, privateApi)
      .then(setRule).catch(() => setRule(null)).finally(() => setLoading(false))
  }, [ruleId])

  if (loading) return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: '#9ca3af', padding: '10px 0' }}>
      <Loader2 size={12} style={{ animation: 'spin 1s linear infinite' }} />
      Cargando envío...
    </div>
  )

  if (!rule) return (
    <p style={{ fontSize: 12, color: '#9ca3af', padding: '10px 0' }}>Sin información de envío.</p>
  )

  return (
    <div style={{
      marginTop: 12, display: 'flex', flexWrap: 'wrap', gap: 16,
      background: '#fafaf9', border: '1px solid #e7e5e4',
      borderRadius: 12, padding: '10px 14px',
    }}>
      {[
        { icon: <MapPin size={11} />, text: `${rule.originZone} → ${rule.destinationZone}` },
        { icon: <Truck size={11} />, text: `${rule.durationTime} ${rule.durationTime === 1 ? 'día' : 'días'} est.` },
        { icon: <CreditCard size={11} />, text: formatCOP(rule.price), accent: true },
      ].map(({ icon, text, accent }, i) => (
        <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 5, fontSize: 12, color: accent ? '#d67d1f' : '#78716c' }}>
          <span style={{ color: '#d67d1f' }}>{icon}</span>
          <span style={{ fontWeight: accent ? 700 : 400 }}>{text}</span>
        </div>
      ))}
    </div>
  )
}

// ─── Order item row ──────────────────────────────────────────────────────────

function OrderItemRow({ item, index }) {
  const [expanded, setExpanded] = useState(false)
  const image = item.imageUrls?.[0] ?? item.product?.images?.[0]

  return (
    <div style={{
      display: 'flex', flexDirection: 'column',
      borderBottom: '1px solid #f5f5f4', paddingBottom: 14, marginBottom: 14,
    }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
        <div style={{
          width: 52, height: 52, flexShrink: 0,
          background: '#f7f3ee', borderRadius: 10,
          display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden',
        }}>
          {image
            ? <img src={image} alt={item.name} style={{ width: '100%', height: '100%', objectFit: 'contain', padding: 4 }} />
            : <Package size={18} color="#d4c4b0" />}
        </div>

        <div style={{ flex: 1, minWidth: 0 }}>
          <p style={{ fontSize: 13, fontWeight: 600, color: '#1c1917', marginBottom: 2, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {item.name ?? item.product?.name ?? 'Producto'}
          </p>
          <p style={{ fontSize: 12, color: '#a8a29e' }}>
            {item.quantity?.toString()} {Number(item.quantity) === 1 ? 'unidad' : 'unidades'}
          </p>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 6 }}>
          <p style={{ fontSize: 14, fontWeight: 700, color: '#1c1917' }}>{formatCOP(item.subtotal)}</p>
          <button
            type="button"
            onClick={() => setExpanded(v => !v)}
            style={{
              display: 'flex', alignItems: 'center', gap: 4,
              fontSize: 11, color: '#d67d1f', fontWeight: 600,
              background: 'none', border: 'none', cursor: 'pointer', padding: 0,
            }}
          >
            <Truck size={11} />
            Envío
            <span style={{ transform: expanded ? 'rotate(90deg)' : 'none', display: 'inline-flex', transition: 'transform 0.15s' }}>
              <ChevronRight size={11} />
            </span>
          </button>
        </div>
      </div>

      {expanded && <ShippingDetail ruleId={item.ruleId} />}
    </div>
  )
}

// ─── Order card ──────────────────────────────────────────────────────────────

function OrderCard({ order, onClick, index }) {
  const images = (order.items ?? []).slice(0, 3).map(i => i.imageUrls?.[0] ?? i.product?.images?.[0]).filter(Boolean)
  const itemCount = order.items?.length ?? 0

  return (
    <button
      type="button"
      onClick={onClick}
      style={{
        width: '100%', textAlign: 'left', cursor: 'pointer',
        background: '#fff', border: '1px solid #e7e5e4',
        borderRadius: 16, padding: '18px 20px',
        display: 'flex', alignItems: 'center', gap: 16,
        transition: 'border-color 0.15s, box-shadow 0.15s',
      }}
      onMouseEnter={e => { e.currentTarget.style.borderColor = '#d67d1f'; e.currentTarget.style.boxShadow = '0 2px 12px rgba(214,125,31,0.08)' }}
      onMouseLeave={e => { e.currentTarget.style.borderColor = '#e7e5e4'; e.currentTarget.style.boxShadow = 'none' }}
    >
      {/* Image stack */}
      <div style={{ position: 'relative', width: 52, height: 52, flexShrink: 0 }}>
        {images.length > 0 ? images.slice(0, 2).map((src, i) => (
          <div key={i} style={{
            position: i === 0 ? 'relative' : 'absolute',
            top: i * -4, left: i * 4,
            width: 48, height: 48, borderRadius: 10,
            background: '#f7f3ee', border: '1.5px solid #fff',
            overflow: 'hidden', display: 'flex', alignItems: 'center', justifyContent: 'center',
            zIndex: 2 - i,
          }}>
            <img src={src} alt="" style={{ width: '100%', height: '100%', objectFit: 'contain', padding: 4 }} />
          </div>
        )) : (
          <div style={{ width: 48, height: 48, borderRadius: 10, background: '#fef3e2', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Package size={20} color="#d67d1f" />
          </div>
        )}
      </div>

      {/* Info */}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
          <p style={{ fontSize: 11, fontWeight: 700, color: '#a8a29e', letterSpacing: '0.08em', textTransform: 'uppercase' }}>
            #{String(order.id).slice(0, 8)}
          </p>
          <StatusPill status={order.status} />
        </div>
        <p style={{ fontSize: 13, fontWeight: 600, color: '#1c1917', marginBottom: 2 }}>
          {itemCount} {itemCount === 1 ? 'producto' : 'productos'}
        </p>
        <p style={{ fontSize: 11, color: '#a8a29e' }}>{formatDate(order.createdAt)}</p>
      </div>

      {/* Amount */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4 }}>
        <p style={{ fontSize: 16, fontWeight: 800, color: '#1c1917' }}>{formatCOP(order.totalAmount)}</p>
        <ChevronRight size={15} color="#d4c4b0" />
      </div>
    </button>
  )
}

// ─── Order detail ────────────────────────────────────────────────────────────

function OrderDetail({ orderId, onBack }) {
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getOrderById(orderId).then(setOrder).catch(() => setOrder(null)).finally(() => setLoading(false))
  }, [orderId])

  if (loading) return (
    <div style={{ display: 'flex', justifyContent: 'center', padding: '80px 0' }}>
      <div style={{ width: 32, height: 32, borderRadius: '50%', border: '3px solid #f5d98b', borderTopColor: '#d67d1f', animation: 'spin 0.8s linear infinite' }} />
    </div>
  )

  if (!order) return (
    <p style={{ textAlign: 'center', padding: '60px 0', fontSize: 14, color: '#a8a29e' }}>No se pudo cargar la orden.</p>
  )

  const subtotal = (order.items ?? []).reduce((s, i) => s + Number(i.subtotal ?? 0), 0)
  const shipping = Number(order.totalAmount ?? 0) - subtotal

  return (
    <div>
      {/* Back + header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 24 }}>
        <button
          type="button"
          onClick={onBack}
          style={{
            width: 36, height: 36, borderRadius: '50%',
            border: '1px solid #e7e5e4', background: '#fff',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            cursor: 'pointer', flexShrink: 0,
          }}
        >
          <ArrowLeft size={15} color="#78716c" />
        </button>
        <div style={{ flex: 1 }}>
          <p style={{ fontSize: 11, fontWeight: 700, color: '#a8a29e', letterSpacing: '0.08em', textTransform: 'uppercase' }}>
            Orden #{String(order.id).slice(0, 8)}
          </p>
          <p style={{ fontSize: 12, color: '#a8a29e', marginTop: 1 }}>{formatDate(order.createdAt)}</p>
        </div>
        <StatusPill status={order.status} />
      </div>

      {/* Summary strip */}
      <div style={{
        display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)',
        gap: 1, background: '#e7e5e4', borderRadius: 14,
        overflow: 'hidden', marginBottom: 20,
      }}>
        {[
          { label: 'Productos', value: formatCOP(subtotal) },
          { label: 'Envío', value: formatCOP(shipping > 0 ? shipping : 0) },
          { label: 'Total', value: formatCOP(order.totalAmount), accent: true },
        ].map(({ label, value, accent }) => (
          <div key={label} style={{ background: '#fff', padding: '14px 16px' }}>
            <p style={{ fontSize: 11, color: '#a8a29e', fontWeight: 600, marginBottom: 4, textTransform: 'uppercase', letterSpacing: '0.06em' }}>{label}</p>
            <p style={{ fontSize: 15, fontWeight: 800, color: accent ? '#d67d1f' : '#1c1917' }}>{value}</p>
          </div>
        ))}
      </div>

      {/* Items */}
      <div style={{ background: '#fff', border: '1px solid #e7e5e4', borderRadius: 14, padding: '18px 20px', marginBottom: 16 }}>
        <p style={{ fontSize: 11, fontWeight: 700, color: '#a8a29e', letterSpacing: '0.08em', textTransform: 'uppercase', marginBottom: 16 }}>
          {order.items?.length ?? 0} {order.items?.length === 1 ? 'producto' : 'productos'}
        </p>
        {(order.items ?? []).map((item, i) => (
          <OrderItemRow key={item.id ?? i} item={item} index={i} />
        ))}
      </div>

      {/* Failed CTA */}
      {order.status === 'FAILED' && (
        <div style={{
          display: 'flex', alignItems: 'flex-start', gap: 12,
          background: '#fef2f2', border: '1px solid #fca5a5',
          borderRadius: 12, padding: '14px 16px',
        }}>
          <AlertTriangle size={15} color="#ef4444" style={{ flexShrink: 0, marginTop: 1 }} />
          <div>
            <p style={{ fontSize: 13, fontWeight: 700, color: '#991b1b' }}>Pago rechazado</p>
            <p style={{ fontSize: 12, color: '#f87171', marginTop: 2 }}>Vuelve al catálogo y realiza una nueva compra.</p>
          </div>
        </div>
      )}
    </div>
  )
}

// ─── Payment banner ──────────────────────────────────────────────────────────

function PaymentBanner({ wompiRef, method }) {
  const [result, setResult] = useState(null)
  const [checking, setChecking] = useState(true)
  const privateApi = usePrivateAxios()

  useEffect(() => {
    if (!wompiRef) return
    let attempts = 0
    const poll = async () => {
      try {
        const data = await getPaymentByReference(wompiRef, privateApi)
        setResult(data)
        if (data.status === 'PENDING' && attempts < 10) { attempts++; setTimeout(poll, 3000) }
        else setChecking(false)
      } catch { setChecking(false) }
    }
    poll()
  }, [wompiRef])

  if (!wompiRef) return null

  const banners = {
    pending:  { bg: '#fef9ee', border: '#f5d98b', color: '#92610a', icon: <Loader2 size={15} style={{ animation: 'spin 1s linear infinite' }} />, text: method === 'NEQUI' ? 'Esperando aprobación en tu app Nequi...' : 'Verificando tu pago...' },
    approved: { bg: '#f0fdf4', border: '#86efac', color: '#166534', icon: <CheckCircle size={15} />, text: '¡Pago aprobado! Tu orden está siendo procesada.' },
    failed:   { bg: '#fef2f2', border: '#fca5a5', color: '#991b1b', icon: <XCircle size={15} />, text: 'El pago fue rechazado. Intenta con otro método.' },
    default:  { bg: '#f9fafb', border: '#e5e7eb', color: '#6b7280', icon: <Clock size={15} />, text: 'Pago en proceso. Recibirás confirmación pronto.' },
  }

  const b = checking ? banners.pending
    : result?.status === 'APPROVED' ? banners.approved
    : (result?.status === 'DECLINED' || result?.status === 'ERROR') ? banners.failed
    : banners.default

  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 10,
      background: b.bg, border: `1px solid ${b.border}`,
      borderRadius: 12, padding: '12px 16px',
      fontSize: 13, color: b.color, fontWeight: 500,
      marginBottom: 20,
    }}>
      {b.icon}
      {b.text}
    </div>
  )
}

// ─── Main ────────────────────────────────────────────────────────────────────

const FILTERS = [
  { key: 'ALL', label: 'Todas' },
  { key: 'PENDING', label: 'Pendientes' },
  { key: 'APPROVED', label: 'Aprobadas' },
  { key: 'CONFIRMED', label: 'Confirmadas' },
  { key: 'FAILED', label: 'Fallidas' },
]

export default function Orders() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { token } = useSelector((state) => state.auth)
  const privateApi = usePrivateAxios()

  const wompiRef = searchParams.get('ref')
  const method = searchParams.get('method')

  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedOrderId, setSelectedOrderId] = useState(null)
  const [statusFilter, setStatusFilter] = useState('ALL')

  useEffect(() => {
    if (!token) { navigate('/account'); return }
    getOrdersByUser(privateApi)
      .then(data => setOrders(data ?? []))
      .catch(() => setOrders([]))
      .finally(() => setLoading(false))
  }, [token])

  const filtered = statusFilter === 'ALL' ? orders : orders.filter(o => o.status === statusFilter)

  if (!token) return null

  return (
    <>
      <style>{`@keyframes spin { to { transform: rotate(360deg) } }`}</style>

      <div style={{ minHeight: '100vh', background: '#fafaf9' }}>
        <div style={{ maxWidth: 720, margin: '0 auto', padding: '40px 20px' }}>

          {!selectedOrderId && (
            <>
              {/* Header */}
              <div style={{ marginBottom: 28 }}>
                <p style={{ fontSize: 11, fontWeight: 700, color: '#d67d1f', letterSpacing: '0.1em', textTransform: 'uppercase', marginBottom: 6 }}>
                  Tu cuenta
                </p>
                <h1 style={{ fontSize: 26, fontWeight: 800, color: '#1c1917', margin: 0 }}>Mis órdenes</h1>
                <p style={{ fontSize: 13, color: '#a8a29e', marginTop: 6 }}>
                  Revisa el estado de tus compras y la información de envío.
                </p>
              </div>

              <PaymentBanner wompiRef={wompiRef} method={method} />

              {/* Filters */}
              <div style={{ display: 'flex', gap: 6, overflowX: 'auto', paddingBottom: 4, marginBottom: 20 }}>
                {FILTERS.map(({ key, label }) => (
                  <button
                    key={key}
                    type="button"
                    onClick={() => setStatusFilter(key)}
                    style={{
                      flexShrink: 0, padding: '6px 14px', borderRadius: 99,
                      fontSize: 12, fontWeight: 600, cursor: 'pointer',
                      border: statusFilter === key ? 'none' : '1px solid #e7e5e4',
                      background: statusFilter === key ? '#1c1917' : '#fff',
                      color: statusFilter === key ? '#fff' : '#78716c',
                      transition: 'all 0.15s',
                    }}
                  >
                    {label}
                    {key === 'ALL' && orders.length > 0 && (
                      <span style={{
                        marginLeft: 6, background: statusFilter === key ? 'rgba(255,255,255,0.2)' : '#f5f5f4',
                        color: statusFilter === key ? '#fff' : '#a8a29e',
                        borderRadius: 99, padding: '1px 7px', fontSize: 11,
                      }}>
                        {orders.length}
                      </span>
                    )}
                  </button>
                ))}
              </div>
            </>
          )}

          {/* Content */}
          {loading ? (
            <div style={{ display: 'flex', justifyContent: 'center', padding: '80px 0' }}>
              <div style={{ width: 32, height: 32, borderRadius: '50%', border: '3px solid #f5d98b', borderTopColor: '#d67d1f', animation: 'spin 0.8s linear infinite' }} />
            </div>
          ) : selectedOrderId ? (
            <OrderDetail orderId={selectedOrderId} onBack={() => setSelectedOrderId(null)} />
          ) : filtered.length === 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12, padding: '80px 0', textAlign: 'center' }}>
              <div style={{ width: 60, height: 60, borderRadius: '50%', background: '#fef3e2', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <ShoppingBag size={24} color="#d67d1f" />
              </div>
              <p style={{ fontSize: 15, fontWeight: 700, color: '#1c1917' }}>
                {statusFilter === 'ALL' ? 'Todavía no tienes órdenes' : `Sin órdenes "${FILTERS.find(f => f.key === statusFilter)?.label}"`}
              </p>
              <p style={{ fontSize: 13, color: '#a8a29e' }}>Cuando realices una compra, aparecerá aquí.</p>
              <Link
                to="/catalog"
                style={{
                  marginTop: 8, background: '#1c1917', color: '#fff',
                  borderRadius: 99, padding: '10px 24px',
                  fontSize: 13, fontWeight: 600, textDecoration: 'none',
                }}
              >
                Ir al catálogo
              </Link>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {filtered.map((order, i) => (
                <OrderCard key={order.id} order={order} index={i} onClick={() => setSelectedOrderId(order.id)} />
              ))}
            </div>
          )}

        </div>
      </div>
    </>
  )
}