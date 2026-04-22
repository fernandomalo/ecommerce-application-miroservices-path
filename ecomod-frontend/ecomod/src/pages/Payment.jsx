import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useSelector } from 'react-redux'
import { CreditCard, Building2, Smartphone, XCircle, Loader2, ShieldCheck } from 'lucide-react'
import { getPaymentByOrder, getPaymentConfig, initiatePayment, getPendingPayment } from '../services/api'
import { usePrivateAxios } from '../hooks/usePrivateAxios'

function formatCOP(usdAmount) {
    // amount is stored in your system — adjust multiplier if already in COP
    return new Intl.NumberFormat('es-CO', {
        style: 'currency',
        currency: 'COP',
        minimumFractionDigits: 0,
    }).format(Number(usdAmount ?? 0))
}

const METHODS = [
    { key: 'CARD', label: 'Tarjeta', icon: CreditCard, description: 'Crédito o débito' },
    { key: 'PSE', label: 'PSE', icon: Building2, description: 'Débito bancario' },
    { key: 'NEQUI', label: 'Nequi', icon: Smartphone, description: 'App móvil' },
]

const PSE_BANKS = [
    { code: '1007', name: 'Bancolombia' },
    { code: '1022', name: 'Banco de Bogotá' },
    { code: '1040', name: 'Banco Agrario' },
    { code: '1032', name: 'Banco Caja Social' },
    { code: '1006', name: 'Banco Davivienda' },
    { code: '1009', name: 'Citibank' },
    { code: '1023', name: 'Banco de Occidente' },
]

export default function Payment() {
    const [searchParams] = useSearchParams()
    const navigate = useNavigate()
    const orderId = searchParams.get('orderId')
    const { token, email } = useSelector((state) => state.auth)
    const privateApi = usePrivateAxios();

    const [paymentInfo, setPaymentInfo] = useState(null)
    const [publicKey, setPublicKey] = useState(null)
    const [loadingInfo, setLoadingInfo] = useState(true)
    const [selectedMethod, setSelectedMethod] = useState('CARD')
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState(null)

    // Wompi widget state
    // Card form
    const [cardToken, setCardToken] = useState(null)
    const [cardTokenizing, setCardTokenizing] = useState(false)
    const [cardTokenized, setCardTokenized] = useState(false)
    const [cardNumber, setCardNumber] = useState('')
    const [cardHolder, setCardHolder] = useState('')
    const [cardExpMonth, setCardExpMonth] = useState('')
    const [cardExpYear, setCardExpYear] = useState('')
    const [cardCvc, setCardCvc] = useState('')
    const [acceptanceToken, setAcceptanceToken] = useState(null)

    // PSE
    const [bank, setBank] = useState('')
    const [userType, setUserType] = useState('0')
    const [legalId, setLegalId] = useState('')
    const [legalIdType, setLegalIdType] = useState('CC')

    // NEQUI
    const [phone, setPhone] = useState('')
    const [installments, setInstallments] = useState(1)

    // Remove orderId from searchParams entirely. The page just loads and polls
    // for the user's pending payment.

    useEffect(() => {
        if (!token) { navigate('/account'); return }

        let attempts = 0

        const poll = async () => {
            try {
                const [info, config] = await Promise.all([
                    getPendingPayment(privateApi),
                    getPaymentConfig(privateApi),
                ])
                setPaymentInfo(info)
                setPublicKey(config.publicKey)

                if (info.status === 'APPROVED') {
                    navigate('/orders?paid=true', { replace: true })
                }
                setLoadingInfo(false)
            } catch {
                // Payment event may not have arrived yet — keep polling briefly
                if (attempts < 6) {
                    attempts++
                    setTimeout(poll, 1500)
                } else {
                    setPaymentInfo(null)
                    setLoadingInfo(false)
                }
            }
        }

        poll()
    }, [token, navigate])

    // Load Wompi tokenization script and set up the card form
    useEffect(() => {
        if (!publicKey || selectedMethod !== 'CARD') return

        const existing = document.getElementById('wompi-tokenizer-script')
        if (existing) {
            initWompiForm(publicKey)
            return
        }

        const script = document.createElement('script')
        script.id = 'wompi-tokenizer-script'
        script.src = 'https://js.wompi.co/v1/'
        script.async = true
        script.onload = () => initWompiForm(publicKey)
        document.head.appendChild(script)
    }, [publicKey, selectedMethod])

    const initWompiForm = (key) => {
        if (!window.WidgetCheckout) return
        setCardTokenLoading(true)
        setCardToken(null)

        const form = new window.WidgetCheckout({
            currency: 'COP',
            amountInCents: Math.round(Number(paymentInfo?.amount ?? 0) * 100),
            reference: 'tokenize-' + Date.now(),
            publicKey: key,
            redirectUrl: window.location.href,
            onLoad: () => setCardTokenLoading(false),
        })

        // Mount into our container
        if (wompiFormRef.current) {
            wompiFormRef.current.innerHTML = ''
            form.open()
        }
    }

    // Listen for Wompi token postMessage
    useEffect(() => {
        const handler = (event) => {
            if (event.data?.type === 'WOMPI_TOKEN' || event.data?.token) {
                setCardToken(event.data.token ?? event.data.data?.token)
            }
        }
        window.addEventListener('message', handler)
        return () => window.removeEventListener('message', handler)
    }, [])

    const handleSubmit = async (e) => {
        e.preventDefault()
        setSubmitting(true)
        setError(null)

        const payload = { paymentMethod: selectedMethod }

        if (selectedMethod === 'CARD') {
            if (!cardToken) {
                setError('Completa los datos de tu tarjeta en el formulario.')
                setSubmitting(false)
                return
            }
            payload.cardToken = cardToken
            payload.installments = installments
            payload.acceptanceToken = acceptanceToken
            payload.email = email
        }

        if (selectedMethod === 'PSE') {
            if (!bank || !legalId) {
                setError('Completa todos los campos de PSE.')
                setSubmitting(false)
                return
            }
            payload.financialInstitutionCode = bank
            payload.userType = userType
            payload.email = email
            payload.userLegalId = legalId
            payload.userLegalIdType = legalIdType
        }

        if (selectedMethod === 'NEQUI') {
            if (!phone || phone.length < 10) {
                setError('Ingresa un número de celular válido.')
                setSubmitting(false)
                return
            }
            payload.phoneNumber = phone
            payload.email = email
        }

        try {
            const result = await initiatePayment(paymentInfo.orderId, payload, privateApi)

            if (selectedMethod === 'PSE' && result.asyncPaymentUrl) {
                window.location.href = result.asyncPaymentUrl
                return
            }

            navigate(`/orders?ref=${result.reference}&method=${selectedMethod}`)
        } catch (err) {
            const msg = err?.response?.data?.message ?? err?.response?.data
            setError(typeof msg === 'string' ? msg : 'Error al procesar el pago. Intenta de nuevo.')
        } finally {
            setSubmitting(false)
        }
    }

    const tokenizeCard = async () => {
        setCardTokenizing(true)
        setCardTokenized(false)
        setError(null)
        try {
            // Step 1: get acceptance_token from Wompi
            const merchantRes = await fetch(
                `https://sandbox.wompi.co/v1/merchants/${publicKey}`
            )
            const merchantData = await merchantRes.json()
            const acceptanceToken =
                merchantData?.data?.presigned_acceptance?.acceptance_token

            if (!acceptanceToken) {
                setError('No se pudo obtener el token de aceptación de Wompi.')
                return
            }

            // Step 2: tokenize the card
            const res = await fetch('https://sandbox.wompi.co/v1/tokens/cards', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${publicKey}`,
                },
                body: JSON.stringify({
                    number: cardNumber.replace(/\s/g, ''),
                    cvc: cardCvc,
                    exp_month: cardExpMonth.padStart(2, '0'),
                    exp_year: cardExpYear,
                    card_holder: cardHolder,
                }),
            })
            const data = await res.json()

            if (data?.status === 'CREATED' && data?.data?.id) {
                setCardToken(data.data.id)
                setCardTokenized(true)
                // Store acceptance_token to send to backend with the payment
                setAcceptanceToken(acceptanceToken)
            } else {
                setError('No pudimos tokenizar la tarjeta. Verifica los datos.')
            }
        } catch {
            setError('Error al conectar con el servicio de pagos.')
        } finally {
            setCardTokenizing(false)
        }
    }

    if (loadingInfo) {
        return (
            <div className="flex justify-center py-24">
                <div className="h-10 w-10 animate-spin rounded-full border-4 border-orange-500 border-t-transparent" />
            </div>
        )
    }

    if (!paymentInfo) {
        return (
            <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 text-center">
                <XCircle size={44} className="text-red-400" />
                <p className="text-base font-semibold text-gray-800">No encontramos una orden pendiente de pago.</p>
                <button
                    type="button"
                    onClick={() => navigate('/catalog')}
                    className="rounded-2xl bg-orange-500 px-6 py-3 text-sm font-semibold text-white hover:bg-orange-600"
                >
                    Ir al catálogo
                </button>
            </div>
        )
    }

    return (
        <div className="min-h-screen bg-gray-50">
            <div className="mx-auto max-w-xl px-4 py-12">

                {/* Header */}
                <div className="mb-8">
                    <p className="text-xs font-semibold uppercase tracking-widest text-orange-500">Pago seguro</p>
                    <h1 className="mt-1 text-2xl font-black text-gray-900">Completa tu compra</h1>
                    <p className="mt-1 text-sm text-gray-500">
                        <p className="mt-1 text-sm text-gray-500">
                            Total a pagar:{' '}
                            <span className="font-bold text-orange-500">{formatCOP(paymentInfo.amount)}</span>
                        </p>
                    </p>
                </div>

                {/* Method tabs */}
                <div className="mb-5 grid grid-cols-3 gap-2">
                    {METHODS.map(({ key, label, icon: Icon, description }) => (
                        <button
                            key={key}
                            type="button"
                            onClick={() => { setSelectedMethod(key); setError(null); setCardToken(null) }}
                            className={`flex flex-col items-center gap-1.5 rounded-2xl border-2 px-2 py-4 text-center transition ${selectedMethod === key
                                ? 'border-orange-500 bg-orange-50'
                                : 'border-gray-200 bg-white hover:border-gray-300'
                                }`}
                        >
                            <Icon size={20} className={selectedMethod === key ? 'text-orange-500' : 'text-gray-400'} />
                            <p className={`text-xs font-bold ${selectedMethod === key ? 'text-orange-600' : 'text-gray-700'}`}>
                                {label}
                            </p>
                            <p className="text-[10px] text-gray-400">{description}</p>
                        </button>
                    ))}
                </div>

                {/* Form card */}
                <div className="rounded-3xl border border-gray-200 bg-white p-6 shadow-sm">
                    <form onSubmit={handleSubmit} className="flex flex-col gap-5">

                        {/* ── CARD ── */}
                        {selectedMethod === 'CARD' && (
                            <div className="flex flex-col gap-4">
                                <div className="flex flex-col gap-1">
                                    <label className="text-xs font-semibold text-gray-500">Número de tarjeta</label>
                                    <input
                                        type="text"
                                        value={cardNumber}
                                        onChange={(e) => {
                                            const val = e.target.value.replace(/\D/g, '').slice(0, 16)
                                            setCardNumber(val.replace(/(.{4})/g, '$1 ').trim())
                                            setCardTokenized(false)
                                            setCardToken(null)
                                        }}
                                        placeholder="1234 5678 9012 3456"
                                        maxLength={19}
                                        className="rounded-2xl border border-gray-200 px-4 py-3 text-sm tracking-widest outline-none focus:border-orange-400"
                                    />
                                </div>
                                <div className="flex flex-col gap-1">
                                    <label className="text-xs font-semibold text-gray-500">Nombre en la tarjeta</label>
                                    <input
                                        type="text"
                                        value={cardHolder}
                                        onChange={(e) => { setCardHolder(e.target.value); setCardTokenized(false); setCardToken(null) }}
                                        placeholder="JUAN PÉREZ"
                                        className="rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-orange-400"
                                    />
                                </div>
                                <div className="grid grid-cols-3 gap-3">
                                    <div className="flex flex-col gap-1">
                                        <label className="text-xs font-semibold text-gray-500">Mes exp.</label>
                                        <input
                                            type="text"
                                            value={cardExpMonth}
                                            onChange={(e) => { setCardExpMonth(e.target.value.replace(/\D/g, '').slice(0, 2)); setCardTokenized(false); setCardToken(null) }}
                                            placeholder="MM"
                                            maxLength={2}
                                            className="rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-orange-400"
                                        />
                                    </div>
                                    <div className="flex flex-col gap-1">
                                        <label className="text-xs font-semibold text-gray-500">Año exp.</label>
                                        <input
                                            type="text"
                                            value={cardExpYear}
                                            onChange={(e) => { setCardExpYear(e.target.value.replace(/\D/g, '').slice(0, 2)); setCardTokenized(false); setCardToken(null) }}
                                            placeholder="AA"
                                            maxLength={2}
                                            className="rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-orange-400"
                                        />
                                    </div>
                                    <div className="flex flex-col gap-1">
                                        <label className="text-xs font-semibold text-gray-500">CVC</label>
                                        <input
                                            type="text"
                                            value={cardCvc}
                                            onChange={(e) => { setCardCvc(e.target.value.replace(/\D/g, '').slice(0, 4)); setCardTokenized(false); setCardToken(null) }}
                                            placeholder="123"
                                            maxLength={4}
                                            className="rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-orange-400"
                                        />
                                    </div>
                                </div>

                                {/* Tokenize button */}
                                {!cardTokenized ? (
                                    <button
                                        type="button"
                                        disabled={cardTokenizing || !cardNumber || !cardHolder || !cardExpMonth || !cardExpYear || !cardCvc}
                                        onClick={tokenizeCard}
                                        className="rounded-2xl border border-orange-500 px-4 py-3 text-sm font-semibold text-orange-600 transition hover:bg-orange-50 disabled:cursor-not-allowed disabled:border-gray-200 disabled:text-gray-400"
                                    >
                                        {cardTokenizing
                                            ? <span className="flex items-center justify-center gap-2"><Loader2 size={14} className="animate-spin" />Verificando tarjeta...</span>
                                            : 'Verificar tarjeta'}
                                    </button>
                                ) : (
                                    <div className="flex items-center gap-2 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
                                        <ShieldCheck size={16} className="shrink-0" />
                                        Tarjeta verificada correctamente
                                    </div>
                                )}

                                <div className="flex flex-col gap-1">
                                    <label className="text-xs font-semibold text-gray-500">Cuotas</label>
                                    <select
                                        value={installments}
                                        onChange={(e) => setInstallments(Number(e.target.value))}
                                        className="rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-orange-400"
                                    >
                                        {[1, 2, 3, 6, 12, 24, 36].map((n) => (
                                            <option key={n} value={n}>{n} {n === 1 ? 'cuota' : 'cuotas'}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                        )}

                        {/* ── PSE ── */}
                        {selectedMethod === 'PSE' && (
                            <div className="flex flex-col gap-4">
                                <div className="flex flex-col gap-1">
                                    <label className="text-xs font-semibold text-gray-500">Banco</label>
                                    <select value={bank} onChange={(e) => setBank(e.target.value)}
                                        className="rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-orange-400">
                                        <option value="">Selecciona tu banco</option>
                                        {PSE_BANKS.map((b) => (
                                            <option key={b.code} value={b.code}>{b.name}</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="flex flex-col gap-1">
                                    <label className="text-xs font-semibold text-gray-500">Tipo de persona</label>
                                    <select value={userType} onChange={(e) => setUserType(e.target.value)}
                                        className="rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-orange-400">
                                        <option value="0">Natural</option>
                                        <option value="1">Jurídica</option>
                                    </select>
                                </div>
                                <div className="grid grid-cols-2 gap-3">
                                    <div className="flex flex-col gap-1">
                                        <label className="text-xs font-semibold text-gray-500">Tipo de documento</label>
                                        <select value={legalIdType} onChange={(e) => setLegalIdType(e.target.value)}
                                            className="rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-orange-400">
                                            <option value="CC">CC</option>
                                            <option value="CE">CE</option>
                                            <option value="NIT">NIT</option>
                                            <option value="PP">Pasaporte</option>
                                        </select>
                                    </div>
                                    <div className="flex flex-col gap-1">
                                        <label className="text-xs font-semibold text-gray-500">Número de documento</label>
                                        <input type="text" value={legalId} onChange={(e) => setLegalId(e.target.value)}
                                            placeholder="123456789"
                                            className="rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-orange-400" />
                                    </div>
                                </div>
                                <p className="text-xs text-gray-400">
                                    Serás redirigido al portal de tu banco para completar el pago.
                                </p>
                            </div>
                        )}

                        {/* ── NEQUI ── */}
                        {selectedMethod === 'NEQUI' && (
                            <div className="flex flex-col gap-4">
                                <p className="text-sm text-gray-500">
                                    Recibirás una notificación push en tu app de Nequi. Ábrela y aprueba el pago.
                                </p>
                                <div className="flex flex-col gap-1">
                                    <label className="text-xs font-semibold text-gray-500">Número de celular</label>
                                    <input type="tel" value={phone} onChange={(e) => setPhone(e.target.value)}
                                        placeholder="3001234567" maxLength={10}
                                        className="rounded-2xl border border-gray-200 px-4 py-3 text-sm outline-none focus:border-orange-400" />
                                </div>
                            </div>
                        )}

                        {error && (
                            <div className="flex items-start gap-2 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                                <XCircle size={15} className="mt-0.5 shrink-0" />
                                {error}
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={submitting}
                            className="mt-1 rounded-2xl bg-orange-500 px-4 py-4 text-sm font-bold text-white transition hover:bg-orange-600 disabled:cursor-not-allowed disabled:bg-gray-300"
                        >
                            {submitting
                                ? <span className="flex items-center justify-center gap-2"><Loader2 size={15} className="animate-spin" />Procesando...</span>
                                : `Pagar ${formatCOP(paymentInfo.amount)}`}
                        </button>
                    </form>
                </div>

                <p className="mt-5 text-center text-xs text-gray-400">
                    <ShieldCheck size={12} className="mr-1 inline" />
                    Pagos procesados de forma segura por <strong className='text-black'>FerPagosBusiness</strong> 
                </p>
            </div>
        </div>
    )
}