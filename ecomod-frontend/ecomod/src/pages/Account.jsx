import { useMemo, useRef, useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Navigate, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { KeyRound, ShieldCheck, ShoppingBag, Store, UserRound, MailCheck } from 'lucide-react'
import { loginUser, registerUser, verifyUser, resendVerificationCode } from '../services/api'
import { setSession } from '../redux/authSlice'
import { buildSessionFromToken } from '../utils/authSession'
import { mergeCart } from '../services/api'
import { usePrivateAxios } from '../hooks/usePrivateAxios'
import { resolvePostLoginRoute } from '../utils/authRole'
import {
  AppField,
  MetricCard,
  SectionCard,
  StatusBanner,
  WorkspacePage,
} from '../components/WorkspaceUI'

const blankAuth = { email: '', password: '' }

function getAuthCopy(intent, mode) {
  if (mode === 'verify') {
    return {
      title: 'Verifica tu correo',
      description: 'Te enviamos un código de 6 dígitos. Ingrésalo para activar tu cuenta.',
      bullets: [
        'Revisa tu bandeja de entrada.',
        'Si no llega, revisa spam.',
        'Puedes reenviar el código después de 60 segundos.',
      ],
    }
  }

  if (intent === 'seller') {
    return {
      title: mode === 'login' ? 'Entra a tu espacio vendedor' : 'Crea tu acceso para vender',
      description: 'Usas una sola cuenta para entrar, pero la experiencia de venta sigue separada de la compra.',
      bullets: [
        'Tu acceso es el mismo en toda la plataforma.',
        'Si ya vendes, te llevamos directo al panel.',
        'Si aun no vendes, te guiamos paso a paso para abrir tu tienda.',
      ],
    }
  }

  return {
    title: mode === 'login' ? 'Vuelve a tu cuenta EcoMod' : 'Crea tu cuenta en EcoMod',
    description: 'Compra con calma y deja listo tu espacio personal para volver cuando quieras.',
    bullets: [
      'Guardas tus datos para comprar mas rapido.',
      'Tu cuenta personal no se mezcla con el panel vendedor.',
      'Si algun dia quieres vender, abres ese espacio aparte.',
    ],
  }
}

// ─── Subcomponente: inputs del código ─────────────────────────────────────────
function VerifyStep({ email, onVerified, onResend, resendCountdown, resendStatus }) {
  const [digits, setDigits] = useState(Array(6).fill(''))
  const [busy, setBusy] = useState(false)
  const [verifyStatus, setVerifyStatus] = useState('idle') // idle | success | error
  const [errorMsg, setErrorMsg] = useState('')
  const inputRefs = useRef([])

  useEffect(() => { inputRefs.current[0]?.focus() }, [])

  const handleChange = (index, value) => {
    if (!/^\d?$/.test(value)) return
    const next = [...digits]
    next[index] = value
    setDigits(next)
    if (value && index < 5) inputRefs.current[index + 1]?.focus()
  }

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !digits[index] && index > 0)
      inputRefs.current[index - 1]?.focus()
  }

  const handlePaste = (e) => {
    e.preventDefault()
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6)
    const next = [...digits]
    pasted.split('').forEach((ch, i) => (next[i] = ch))
    setDigits(next)
    const nextEmpty = next.findIndex((d) => !d)
    inputRefs.current[nextEmpty !== -1 ? nextEmpty : 5]?.focus()
  }

  const handleVerify = async () => {
    const code = parseInt(digits.join(''), 10)
    setBusy(true)
    setErrorMsg('')
    try {
      await verifyUser(code)
      setVerifyStatus('success')
      setTimeout(() => onVerified(), 1200)
    } catch (err) {
      setVerifyStatus('error')
      setErrorMsg(
        typeof err?.response?.data === 'string'
          ? err.response.data
          : 'El código no es válido o ya expiró.'
      )
      setBusy(false)
    }
  }

  const isComplete = digits.every((d) => d !== '')

  return (
    <div className="space-y-5">
      <p className="text-sm text-[#5f6368]">
        Código enviado a{' '}
        <span className="font-medium text-[#1f2328]">{email}</span>
      </p>

      {/* Inputs */}
      <div className="flex gap-3" onPaste={handlePaste}>
        {digits.map((digit, i) => (
          <input
            key={i}
            ref={(el) => (inputRefs.current[i] = el)}
            type="text"
            inputMode="numeric"
            maxLength={1}
            value={digit}
            onChange={(e) => handleChange(i, e.target.value)}
            onKeyDown={(e) => handleKeyDown(i, e)}
            className={[
              'w-11 h-13 text-center text-xl font-semibold rounded-xl border-2 outline-none transition-all bg-[#faf9f6]',
              verifyStatus === 'error'
                ? 'border-red-400 text-red-600 bg-red-50'
                : digit
                  ? 'border-amber-500 text-[#1f2328]'
                  : 'border-stone-200 text-[#1f2328] focus:border-amber-400 focus:bg-white',
            ].join(' ')}
          />
        ))}
      </div>

      {/* Error / Success */}
      {errorMsg && (
        <p className="text-sm text-red-500">{errorMsg}</p>
      )}
      {verifyStatus === 'success' && (
        <div className="rounded-2xl bg-green-50 px-4 py-3 text-sm text-green-700">
          ¡Cuenta verificada! Iniciando sesión...
        </div>
      )}

      {/* Botón verificar */}
      <button
        onClick={handleVerify}
        disabled={!isComplete || busy || verifyStatus === 'success'}
        className="rounded-2xl bg-[#1f2328] px-4 py-3 text-sm font-semibold text-white hover:bg-[#2b3037] disabled:bg-gray-300"
      >
        {busy ? 'Verificando...' : 'Verificar cuenta'}
      </button>

      {/* Reenviar */}
      <p className="text-sm text-[#5f6368]">
        ¿No recibiste el código?{' '}
        <button
          onClick={onResend}
          disabled={resendCountdown > 0 || resendStatus === 'sending'}
          className={`font-medium transition-colors ${resendCountdown > 0 || resendStatus === 'sending'
            ? 'text-gray-400 cursor-not-allowed'
            : 'text-amber-600 hover:text-amber-700'
            }`}
        >
          {resendStatus === 'sending'
            ? 'Enviando...'
            : resendCountdown > 0
              ? `Reenviar en ${resendCountdown}s`
              : 'Reenviar código'}
        </button>
      </p>
    </div>
  )
}

// ─── Página principal ──────────────────────────────────────────────────────────
export default function Account() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams] = useSearchParams()
  const { token, roles } = useSelector((state) => state.auth)
  const privateApi = usePrivateAxios();

  const [mode, setMode] = useState('login')         // login | register | verify
  const [authForm, setAuthForm] = useState(blankAuth)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState(null)
  const [resendCountdown, setResendCountdown] = useState(0)
  const [resendStatus, setResendStatus] = useState('idle')
  const [verifyEmail, setVerifyEmail] = useState('')
  const intent = searchParams.get('intent') === 'seller' ? 'seller' : 'buyer'
  const requestedPath = location.state?.from?.pathname
  const copy = getAuthCopy(intent, mode)

  // countdown para reenvío
  useEffect(() => {
    if (resendCountdown <= 0) return
    const t = setTimeout(() => setResendCountdown((c) => c - 1), 1000)
    return () => clearTimeout(t)
  }, [resendCountdown])

  const metrics = useMemo(() => ([
    { label: 'Acceso', value: 'Unificado', icon: KeyRound, hint: 'Una sola cuenta para entrar a toda la experiencia.' },
    { label: 'Modo', value: intent === 'seller' ? 'Venta' : 'Compra', icon: intent === 'seller' ? Store : ShoppingBag, hint: 'La ruta que sigues despues del login depende de lo que quieres hacer.' },
    { label: 'Cuenta', value: mode === 'login' ? 'Entrar' : mode === 'verify' ? 'Verificar' : 'Crear', icon: mode === 'verify' ? MailCheck : UserRound, hint: 'Puedes cambiar entre entrar y crear cuenta desde la misma vista.' },
  ]), [intent, mode])

  if (token) {
    return (
      <Navigate
        to={resolvePostLoginRoute({ requestedPath, intent, rawRoles: roles })}
        replace
      />
    )
  }

  // ── Registro → verificación ──────────────────────────────────────────────────
  const handleSubmit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setMessage(null)

    try {
      if (mode === 'register') {
        await registerUser(authForm)

        setVerifyEmail(authForm.email) // 🔥 GUARDAMOS EL EMAIL
        setResendCountdown(60)
        setMode('verify')
        setBusy(false)
        return
      }

      const response = await loginUser(authForm)
      const session = buildSessionFromToken(response.accessToken, response.roles)
      try {
        await mergeCart(response.accessToken)
      } catch {
        // no anonymous cart to merge, that's fine
      }
      dispatch(setSession(session))   // dispatch AFTER merge completes
      setAuthForm(blankAuth)
      navigate(
        resolvePostLoginRoute({ requestedPath, intent, rawRoles: session.roles }),
        { replace: true }
      )
    } catch (error) {
      setMessage({
        type: 'error',
        text: typeof error?.response?.data === 'string'
          ? error.response.data
          : 'No pudimos completar tu ingreso.',
      })
    } finally {
      setBusy(false)
    }
  }

  // ── Después de verificar → login automático ──────────────────────────────────
  const handleVerified = async () => {
    try {
      const response = await loginUser(authForm)
      const session = buildSessionFromToken(response.accessToken, response.roles)
      dispatch(setSession(session))
      setAuthForm(blankAuth)
      navigate(
        resolvePostLoginRoute({ requestedPath, intent, rawRoles: session.roles }),
        { replace: true }
      )
    } catch {
      // Si el auto-login falla, mandamos al login manual
      setMode('login')
      setMessage({ type: 'error', text: 'Cuenta verificada. Inicia sesión para continuar.' })
    }
  }

  // ── Reenviar código ──────────────────────────────────────────────────────────
  const handleResend = async () => {
    console.log("REENVIANDO A:", verifyEmail)

    if (resendCountdown > 0 || resendStatus === 'sending') return
    setResendStatus('sending')
    try {
      await resendVerificationCode(verifyEmail)
      setResendCountdown(60)
    } catch (err) {
      console.error(err)
    } finally {
      setResendStatus('idle')
    }
  }

  // ── Render ───────────────────────────────────────────────────────────────────
  const tabs = mode === 'verify'
    ? [{ key: 'verify', label: 'Verificar correo', icon: MailCheck }]
    : [
      { key: 'login', label: 'Entrar', icon: KeyRound },
      { key: 'register', label: 'Crear cuenta', icon: ShieldCheck },
    ]

  return (
    <WorkspacePage
      eyebrow="Acceso EcoMod"
      title={copy.title}
      description={copy.description}
      metrics={metrics}
      tabs={tabs}
      activeTab={mode}
      onTabChange={(tab) => { if (mode !== 'verify') setMode(tab) }}
    >
      <div className="grid gap-6 lg:grid-cols-[1.05fr_0.95fr]">

        {/* ── Columna izquierda ── */}
        <SectionCard
          title={
            mode === 'verify'
              ? 'Ingresa tu código'
              : mode === 'login'
                ? 'Accede a tu cuenta'
                : 'Crea tu cuenta'
          }
          description={
            mode === 'verify'
              ? 'Escribe el código de 6 dígitos que llegó a tu correo.'
              : intent === 'seller'
                ? 'Entraras por una sola puerta y despues te llevaremos al camino correcto para vender.'
                : 'Prepara tu cuenta personal para comprar sin mezclarla con herramientas de vendedor.'
          }
          icon={mode === 'verify' ? MailCheck : mode === 'login' ? KeyRound : ShieldCheck}
        >
          {mode === 'verify' ? (
            <VerifyStep
              email={verifyEmail}
              onVerified={handleVerified}
              onResend={handleResend}
              resendCountdown={resendCountdown}
              resendStatus={resendStatus}
            />
          ) : (
            <form className="space-y-4" onSubmit={handleSubmit}>
              <AppField
                label="Email"
                type="email"
                value={authForm.email}
                onChange={(e) => setAuthForm((f) => ({ ...f, email: e.target.value }))}
                placeholder="tu@email.com"
              />
              <AppField
                label="Contrasena"
                type="password"
                value={authForm.password}
                onChange={(e) => setAuthForm((f) => ({ ...f, password: e.target.value }))}
                placeholder="Ingresa tu contrasena"
              />
              <StatusBanner message={message} />
              <button
                type="submit"
                disabled={busy}
                className="rounded-2xl bg-[#1f2328] px-4 py-3 text-sm font-semibold text-white hover:bg-[#2b3037] disabled:bg-gray-300"
              >
                {busy
                  ? 'Preparando tu acceso...'
                  : mode === 'login'
                    ? 'Entrar'
                    : 'Crear cuenta'}
              </button>
            </form>
          )}
        </SectionCard>

        {/* ── Columna derecha ── */}
        <div className="space-y-6">
          <SectionCard
            title={
              mode === 'verify'
                ? 'Por qué verificamos'
                : intent === 'seller'
                  ? 'Asi entra una tienda'
                  : 'Asi funciona tu cuenta'
            }
            description="La idea es que el usuario no tenga que entender detalles tecnicos para moverse con seguridad."
            icon={intent === 'seller' ? Store : ShoppingBag}
          >
            <div className="space-y-3 text-sm text-[#5f6368]">
              {copy.bullets.map((bullet) => (
                <div key={bullet} className="rounded-2xl bg-[#f7f1e8] px-4 py-3">
                  {bullet}
                </div>
              ))}
            </div>
          </SectionCard>

          <SectionCard
            title={intent === 'seller' ? 'Compra y venta, cada una en su lugar' : 'Si luego quieres vender'}
            description={
              intent === 'seller'
                ? 'Tu panel vendedor queda ordenado por pestañas para productos, inventario y categorias.'
                : 'Podras abrir una tienda sin perder la claridad de tu experiencia como comprador.'
            }
            icon={intent === 'seller' ? Store : UserRound}
          >
            <div className="grid gap-4 md:grid-cols-2">
              <MetricCard label="Compra" value="Separada" icon={ShoppingBag} hint="El carrito y la cuenta personal viven en un espacio distinto." />
              <MetricCard label="Venta" value="Ordenada" icon={Store} hint="El panel vendedor tiene su propia navegacion y sus propias tareas." />
            </div>
          </SectionCard>
        </div>

      </div>
    </WorkspacePage>
  )
}