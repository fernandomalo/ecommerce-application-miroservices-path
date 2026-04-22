import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Building2,
  CheckCircle2,
  MapPin,
  Save,
  Store,
  UserRound,
} from 'lucide-react'
import { createCompany, saveUserInfo } from '../services/api'
import { usePrivateAxios } from '../hooks/usePrivateAxios'
import { useRefresh } from '../hooks/useRefresh'
import { useAccountData } from '../hooks/useAccountData'
import {
  AppField,
  EmptyPanel,
  SectionCard,
  StatusBanner,
  WorkspacePage,
} from '../components/WorkspaceUI'

const blankProfile = { country: '', region: '', city: '', location: '' }
const blankCompany = { name: '', country: '', region: '', city: '', location: '' }

function wait(ms) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms)
  })
}

export default function SellerOnboarding() {
  const navigate = useNavigate()
  const privateApi = usePrivateAxios()
  const refresh = useRefresh()
  const { userInfo, company, loading, reload, setUserInfo, setCompany } = useAccountData()
  const [profileForm, setProfileForm] = useState(blankProfile)
  const [companyForm, setCompanyForm] = useState(blankCompany)
  const [activeTab, setActiveTab] = useState('profile')
  const [busy, setBusy] = useState({ profile: false, company: false })
  const [message, setMessage] = useState({ profile: null, company: null })

  useEffect(() => {
    setProfileForm({
      country: userInfo?.country ?? '',
      region: userInfo?.region ?? '',
      city: userInfo?.city ?? '',
      location: userInfo?.location ?? '',
    })
    setCompanyForm((current) => ({
      name: company?.name ?? current.name,
      country: company?.country ?? userInfo?.country ?? '',
      region: company?.region ?? userInfo?.region ?? '',
      city: company?.city ?? userInfo?.city ?? '',
      location: company?.location ?? userInfo?.location ?? '',
    }))
  }, [company, userInfo])

  useEffect(() => {
    if (company) {
      setActiveTab('store')
    }
  }, [company])

  const tabs = useMemo(() => ([
    { key: 'profile', label: 'Paso 1. Datos', icon: UserRound },
    { key: 'store', label: 'Paso 2. Tienda', icon: Store },
  ]), [])

  const metrics = useMemo(() => ([
    { label: 'Paso actual', value: company ? 'Panel listo' : userInfo ? 'Crear tienda' : 'Completar datos', icon: CheckCircle2, hint: 'Tu avance se actualiza automaticamente.' },
    { label: 'Pais base', value: userInfo?.country || 'Pendiente', icon: MapPin, hint: 'La tienda usara esta base como punto de partida.' },
    { label: 'Estado de tienda', value: company ? 'Creada' : 'Pendiente', icon: Building2, hint: 'Una vez creada, te llevamos al panel vendedor.' },
  ]), [company, userInfo])

  const handleProfileSubmit = async (event) => {
    event.preventDefault()
    setBusy((current) => ({ ...current, profile: true }))
    setMessage((current) => ({ ...current, profile: null }))

    try {
      await saveUserInfo(profileForm, privateApi)
      setUserInfo((current) => ({
        ...(current ?? {}),
        ...profileForm,
      }))
      await reload().catch(() => {})
      setMessage((current) => ({
        ...current,
        profile: { type: 'success', text: 'Tus datos quedaron listos.' },
      }))
      setActiveTab('store')
    } catch (error) {
      setMessage((current) => ({
        ...current,
        profile: {
          type: 'error',
          text: typeof error?.response?.data === 'string'
            ? error.response.data
            : 'No pudimos guardar tus datos.',
        },
      }))
    } finally {
      setBusy((current) => ({ ...current, profile: false }))
    }
  }

  const handleCompanySubmit = async (event) => {
    event.preventDefault()
    setBusy((current) => ({ ...current, company: true }))
    setMessage((current) => ({ ...current, company: null }))

    const optimisticCompany = {
      ...(company ?? {}),
      name: companyForm.name.trim(),
      country: companyForm.country.trim(),
      region: companyForm.region.trim(),
      city: companyForm.city.trim(),
      location: companyForm.location.trim(),
    }

    try {
      await createCompany(companyForm, privateApi)
      setCompany(optimisticCompany)

      await refresh().catch(() => {})

      let syncedCompany = null
      for (let attempt = 0; attempt < 3; attempt += 1) {
        const nextAccount = await reload().catch(() => null)
        syncedCompany = nextAccount?.company ?? null

        if (syncedCompany) {
          break
        }

        await wait(350)
      }

      if (!syncedCompany) {
        setCompany(optimisticCompany)
      }

      navigate('/seller', { replace: true })
    } catch (error) {
      setMessage((current) => ({
        ...current,
        company: {
          type: 'error',
          text: typeof error?.response?.data === 'string'
            ? error.response.data
            : 'No pudimos crear tu tienda.',
        },
      }))
    } finally {
      setBusy((current) => ({ ...current, company: false }))
    }
  }

  return (
    <WorkspacePage
      eyebrow="Vender en EcoMod"
      title="Configura tu espacio vendedor sin mezclarlo con la compra"
      description="Tu acceso sigue siendo uno solo, pero aqui trabajas un flujo separado para preparar tu tienda y despues entrar a tu panel."
      metrics={metrics}
      tabs={tabs}
      activeTab={activeTab}
      onTabChange={setActiveTab}
    >
      {activeTab === 'profile' ? (
        <SectionCard
          title="Paso 1. Tus datos"
          description="Necesitamos esta base para abrir la tienda con informacion consistente."
          icon={UserRound}
        >
          {loading ? (
            <div className="flex justify-center py-12">
              <div className="h-10 w-10 animate-spin rounded-full border-4 border-[#d67d1f] border-t-transparent" />
            </div>
          ) : (
            <form className="grid gap-4 md:grid-cols-2" onSubmit={handleProfileSubmit}>
              <AppField label="Pais" value={profileForm.country} onChange={(event) => setProfileForm((current) => ({ ...current, country: event.target.value }))} placeholder="Ej: Colombia" />
              <AppField label="Region" value={profileForm.region} onChange={(event) => setProfileForm((current) => ({ ...current, region: event.target.value }))} placeholder="Ej: Cordoba" />
              <AppField label="Ciudad" value={profileForm.city} onChange={(event) => setProfileForm((current) => ({ ...current, city: event.target.value }))} placeholder="Ej: Cerete" />
              <AppField label="Direccion" value={profileForm.location} onChange={(event) => setProfileForm((current) => ({ ...current, location: event.target.value }))} placeholder="Ej: Cra 13 #16b - 50" />
              <div className="md:col-span-2">
                <StatusBanner message={message.profile} />
                <button type="submit" disabled={busy.profile} className="mt-4 inline-flex items-center gap-2 rounded-2xl bg-[#d67d1f] px-4 py-3 text-sm font-semibold text-white hover:bg-[#bd6c16] disabled:bg-gray-300">
                  <Save size={16} />
                  {busy.profile ? 'Guardando...' : userInfo ? 'Actualizar datos' : 'Guardar datos'}
                </button>
              </div>
            </form>
          )}
        </SectionCard>
      ) : null}

      {activeTab === 'store' ? (
        company ? (
          <EmptyPanel
            title="Tu tienda ya esta lista"
            description={`La tienda ${company.name} ya existe. Puedes entrar directamente al panel y seguir con categorias, productos e inventario.`}
            action={(
              <button type="button" onClick={async () => {
                await refresh().catch(() => {})
                navigate('/seller', { replace: true })
              }} className="inline-flex items-center gap-2 rounded-2xl bg-[#d67d1f] px-4 py-3 text-sm font-semibold text-white hover:bg-[#bd6c16]">
                <Store size={16} />
                Ir a mi panel
              </button>
            )}
          />
        ) : (
          <SectionCard
            title="Paso 2. Tu tienda"
            description="Dale nombre y ubicacion a tu espacio de ventas. Si este paso falla, intentaremos mantener tu avance sin perder contexto."
            icon={Store}
          >
            <form className="grid gap-4 md:grid-cols-2" onSubmit={handleCompanySubmit}>
              <div className="md:col-span-2">
                <AppField label="Nombre de la tienda" value={companyForm.name} onChange={(event) => setCompanyForm((current) => ({ ...current, name: event.target.value }))} placeholder="Ej: Prosystem" />
              </div>
              <AppField label="Pais" value={companyForm.country} onChange={(event) => setCompanyForm((current) => ({ ...current, country: event.target.value }))} placeholder="Ej: Colombia" />
              <AppField label="Region" value={companyForm.region} onChange={(event) => setCompanyForm((current) => ({ ...current, region: event.target.value }))} placeholder="Ej: Cordoba" />
              <AppField label="Ciudad" value={companyForm.city} onChange={(event) => setCompanyForm((current) => ({ ...current, city: event.target.value }))} placeholder="Ej: Cerete" />
              <AppField label="Direccion" value={companyForm.location} onChange={(event) => setCompanyForm((current) => ({ ...current, location: event.target.value }))} placeholder="Ej: Cra 13 #16b - 50" />
              <div className="md:col-span-2">
                <StatusBanner message={message.company} />
                <button type="submit" disabled={busy.company || !userInfo} className="mt-4 inline-flex items-center gap-2 rounded-2xl bg-[#d67d1f] px-4 py-3 text-sm font-semibold text-white hover:bg-[#bd6c16] disabled:bg-gray-300">
                  <Store size={16} />
                  {busy.company ? 'Creando tu tienda...' : 'Crear tienda'}
                </button>
              </div>
            </form>
          </SectionCard>
        )
      ) : null}
    </WorkspacePage>
  )
}
