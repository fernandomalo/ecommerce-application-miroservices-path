import { useEffect, useMemo, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Link, useNavigate } from 'react-router-dom'
import {
  CreditCard,
  LogOut,
  MapPin,
  Save,
  ShoppingBag,
  Smartphone,
  Store,
  UserRound,
} from 'lucide-react'
import { logoutUser, saveUserInfo } from '../services/api'
import { clearSession } from '../redux/authSlice'
import { usePrivateAxios } from '../hooks/usePrivateAxios'
import { useAccountData } from '../hooks/useAccountData'
import {
  AppField,
  MetricCard,
  SectionCard,
  StatusBanner,
  WorkspacePage,
} from '../components/WorkspaceUI'

const blankProfile = {
  country: '',
  countryId: '',
  region: '',
  regionId: '',
  city: '',
  cityId: '',
  location: '',
  phoneNumber: ''
}

export default function BuyerAccount() {
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const privateApi = usePrivateAxios()
  const { email } = useSelector((state) => state.auth)
  const { userInfo, loading, reload, setUserInfo } = useAccountData()
  const [activeTab, setActiveTab] = useState('profile')
  const [profileForm, setProfileForm] = useState(blankProfile)
  const [busy, setBusy] = useState(false)
  const [message, setMessage] = useState(null)
  const [countries, setCountries] = useState([])
  const [regions, setRegions] = useState([])
  const [cities, setCities] = useState([])

  useEffect(() => {
    const fetchCountries = async () => {
      try {
        const res = await privateApi.get('/api/v1/zones/countries')
        setCountries(res.data)
      } catch (err) {
        console.error(err)
      }
    }

    fetchCountries()
  }, [])

  useEffect(() => {
    if (!profileForm.countryId) return

    privateApi.get(`/api/v1/zones/${profileForm.countryId}`)
      .then(res => {
        setRegions(res.data)
        setCities([])
      })
  }, [profileForm.countryId])

  useEffect(() => {
    if (!profileForm.regionId) return

    privateApi.get(`/api/v1/zones/${profileForm.regionId}`)
      .then(res => setCities(res.data))
  }, [profileForm.regionId])

  useEffect(() => {
    if (!userInfo) return

    setProfileForm({
      country: userInfo.country ?? '',
      countryId: userInfo.countryId ?? '',
      region: userInfo.region ?? '',
      regionId: userInfo.regionId ?? '',
      city: userInfo.city ?? '',
      cityId: userInfo.cityId ?? '',
      location: userInfo.location ?? '',
      phoneNumber: userInfo.phoneNumber ?? ''
    })
  }, [userInfo])

  const tabs = useMemo(() => ([
    { key: 'profile', label: 'Perfil', icon: UserRound },
    { key: 'checkout', label: 'Compra', icon: CreditCard },
    { key: 'sell', label: 'Vender', icon: Store },
  ]), [])

  const metrics = useMemo(() => ([
    { label: 'Cuenta', value: 'Comprador', icon: ShoppingBag, hint: 'Tu experiencia de compra esta activa.' },
    { label: 'Pais', value: userInfo?.country || 'Pendiente', icon: MapPin, hint: 'Completa tus datos para agilizar futuras compras.' },
    { label: 'Ciudad', value: userInfo?.city || 'Pendiente', icon: MapPin, hint: 'Tu ubicacion ayuda a estimar entregas.' },
    { label: 'Teléfono', value: userInfo?.phoneNumber || 'Pendiente', icon: Smartphone, hint: 'Tu número de celular ayuda a contactarte.' },
  ]), [userInfo?.city, userInfo?.country, userInfo?.phoneNumber])

  const handleSubmit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setMessage(null)

    try {
      await saveUserInfo(profileForm, privateApi)
      setUserInfo((current) => ({
        ...(current ?? {}),
        ...profileForm,
      }))
      await reload().catch(() => { })
      setMessage({ type: 'success', text: 'Tus datos quedaron guardados.' })
    } catch (error) {
      setMessage({
        type: 'error',
        text: typeof error?.response?.data === 'string'
          ? error.response.data
          : 'No pudimos guardar tus datos.',
      })
    } finally {
      setBusy(false)
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

  return (
    <WorkspacePage
      eyebrow="Mi cuenta"
      title="Tu espacio para comprar sin enredos"
      description="Manten tus datos al dia, revisa tu estado como comprador y entra al modo vendedor solo cuando realmente quieras vender."
      actions={[
        <button key="logout" type="button" onClick={handleLogout} className="inline-flex items-center gap-2 rounded-2xl bg-[#1f2328] px-4 py-3 text-sm font-semibold text-white hover:bg-[#2b3037]">
          <LogOut size={16} />
          Salir
        </button>,
      ]}
      metrics={metrics}
      tabs={tabs}
      activeTab={activeTab}
      onTabChange={setActiveTab}
    >
      {activeTab === 'profile' ? (
        <div className="grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
          <SectionCard
            title="Informacion personal"
            description="Aqui guardas solo lo necesario para comprar mejor."
            icon={UserRound}
          >
            {loading ? (
              <div className="flex justify-center py-12">
                <div className="h-10 w-10 animate-spin rounded-full border-4 border-[#d67d1f] border-t-transparent" />
              </div>
            ) : (
              <form className="grid gap-4 md:grid-cols-2" onSubmit={handleSubmit}>
                {/* País */}
                <label>
                  <span className="text-sm font-medium">País</span>
                  <select
                    value={profileForm.countryId}
                    onChange={(e) => {
                      const selected = countries.find(c => c.id == e.target.value)

                      setProfileForm((c) => ({
                        ...c,
                        country: selected?.name || '',
                        countryId: selected?.id || '',
                        region: '',
                        regionId: '',
                        city: '',
                        cityId: ''
                      }))
                    }}
                    className="w-full rounded-2xl border px-4 py-3"
                  >
                    <option value="">Selecciona país</option>
                    {countries.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </label>

                {/* Región */}
                <label>
                  <span className="text-sm font-medium">Región</span>
                  <select
                    value={profileForm.regionId}
                    onChange={(e) => {
                      const selected = regions.find(r => r.id == e.target.value)

                      setProfileForm((c) => ({
                        ...c,
                        region: selected?.name || '',
                        regionId: selected?.id || '',
                        city: '',
                        cityId: ''
                      }))
                    }}
                    className="w-full rounded-2xl border px-4 py-3"
                  >
                    <option value="">Selecciona región</option>
                    {regions.map((r) => (
                      <option key={r.id} value={r.id}>
                        {r.name}
                      </option>
                    ))}
                  </select>
                </label>

                {/* Ciudad */}
                <label>
                  <span className="text-sm font-medium">Ciudad</span>
                  <select
                    value={profileForm.cityId}
                    onChange={(e) => {
                      const selected = cities.find(c => c.id == e.target.value)

                      setProfileForm((c) => ({
                        ...c,
                        city: selected?.name || '',
                        cityId: selected?.id || ''
                      }))
                    }}
                    className="w-full rounded-2xl border px-4 py-3"
                  >
                    <option value="">Selecciona ciudad</option>
                    {cities.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </label>
                <AppField label="Direccion" value={profileForm.location} onChange={(event) => setProfileForm((current) => ({ ...current, location: event.target.value }))} placeholder="Ej: Cra 13 #16b - 50" />
                <AppField
                  label="Teléfono"
                  value={profileForm.phoneNumber}
                  onChange={(e) =>
                    setProfileForm((c) => ({ ...c, phoneNumber: e.target.value }))
                  }
                  placeholder="Ej: 3001234567"
                />
                <div className="md:col-span-2">
                  <StatusBanner message={message} />
                  <button type="submit" disabled={busy} className="mt-4 inline-flex items-center gap-2 rounded-2xl bg-[#d67d1f] px-4 py-3 text-sm font-semibold text-white hover:bg-[#bd6c16] disabled:bg-gray-300">
                    <Save size={16} />
                    {busy ? 'Guardando...' : userInfo ? 'Actualizar datos' : 'Guardar datos'}
                  </button>
                </div>
              </form>
            )}
          </SectionCard>

          <SectionCard
            title="Resumen"
            description="Un espacio simple para comprar, sin mezclarlo con herramientas de vendedor."
            icon={ShoppingBag}
          >
            <div className="space-y-3 text-sm text-[#5f6368]">
              <div className="rounded-2xl bg-[#f7f1e8] px-4 py-3">
                <strong className="text-[#1f2328]">Email:</strong> {email || 'Sin email'}
              </div>
              <div className="rounded-2xl bg-[#f7f1e8] px-4 py-3">
                <strong className="text-[#1f2328]">Estado:</strong> Cuenta de compra activa
              </div>
              <div className="rounded-2xl bg-[#f7f1e8] px-4 py-3">
                <strong className="text-[#1f2328]">Direccion actual:</strong> {userInfo?.location || 'Aun sin definir'}
              </div>
            </div>
          </SectionCard>
        </div>
      ) : null}

      {activeTab === 'checkout' ? (
        <div className="grid gap-6 lg:grid-cols-3">
          <MetricCard label="Datos listos" value={userInfo?.country ? 'Si' : 'No'} icon={CreditCard} hint="Con datos completos, tus compras quedan mas rapidas." />
          <MetricCard label="Pais de entrega" value={userInfo?.country || 'Pendiente'} icon={MapPin} hint="Usamos estos datos para estimar mejor tus entregas." />
          <MetricCard label="Ciudad" value={userInfo?.city || 'Pendiente'} icon={MapPin} hint="Actualizala cuando cambies de direccion." />
        </div>
      ) : null}

      {activeTab === 'sell' ? (
        <SectionCard
          title="Quieres vender?"
          description="Tu cuenta de compra sigue intacta. Si decides vender, abres un espacio aparte con su propio panel."
          icon={Store}
        >
          <div className="flex flex-col gap-4 text-sm text-[#5f6368]">
            <p>Al crear tu tienda tendras un panel separado para categorias, productos e inventario.</p>
            <div className="flex flex-wrap gap-3">
              <Link to="/sell" className="inline-flex items-center gap-2 rounded-2xl bg-[#d67d1f] px-4 py-3 text-sm font-semibold text-white hover:bg-[#bd6c16]">
                <Store size={16} />
                Empezar a vender
              </Link>
              <Link to="/catalog" className="inline-flex items-center gap-2 rounded-2xl border border-[#ded3c4] px-4 py-3 text-sm font-semibold text-[#4e5358] hover:bg-[#f3ede3]">
                <ShoppingBag size={16} />
                Seguir explorando
              </Link>
            </div>
          </div>
        </SectionCard>
      ) : null}
    </WorkspacePage>
  )
}
