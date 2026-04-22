export function WorkspacePage({
  eyebrow,
  title,
  description,
  actions,
  metrics,
  tabs,
  activeTab,
  onTabChange,
  children,
}) {
  return (
    <div className="min-h-screen bg-[#f6f3ee]">
      <div className="mx-auto flex max-w-7xl flex-col gap-6 px-4 py-8">
        <div className="overflow-hidden rounded-[32px] border border-[#eadfce] bg-[radial-gradient(circle_at_top_left,_rgba(255,188,120,0.35),_transparent_32%),linear-gradient(135deg,_#fff8ef,_#f7efe4_55%,_#f4ebde)] p-8 shadow-[0_28px_80px_rgba(51,35,18,0.08)]">
          <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-3xl">
              {eyebrow ? (
                <p className="text-xs font-semibold uppercase tracking-[0.32em] text-[#b67a2c]">
                  {eyebrow}
                </p>
              ) : null}
              <h1 className="mt-3 text-3xl font-black tracking-tight text-[#1f2328] sm:text-4xl">
                {title}
              </h1>
              {description ? (
                <p className="mt-3 text-sm leading-7 text-[#5f6368]">
                  {description}
                </p>
              ) : null}
            </div>

            {actions ? (
              <div className="flex flex-wrap gap-3">
                {actions}
              </div>
            ) : null}
          </div>
        </div>

        {metrics?.length ? (
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            {metrics.map((metric) => (
              <MetricCard key={metric.label} {...metric} />
            ))}
          </div>
        ) : null}

        {tabs?.length ? (
          <div className="rounded-3xl border border-[#eadfce] bg-[#fffdf9] p-3 shadow-sm">
            <div className="flex flex-wrap gap-2">
              {tabs.map((tab) => {
                const Icon = tab.icon
                const isActive = activeTab === tab.key

                return (
                  <button
                    key={tab.key}
                    type="button"
                    onClick={() => onTabChange(tab.key)}
                    className={`inline-flex items-center gap-2 rounded-2xl px-4 py-3 text-sm font-semibold transition ${
                      isActive
                        ? 'bg-[#1f2328] text-white shadow-sm'
                        : 'bg-[#f3ede3] text-[#4e5358] hover:bg-[#ece3d5]'
                    }`}
                  >
                    {Icon ? <Icon size={16} /> : null}
                    {tab.label}
                  </button>
                )
              })}
            </div>
          </div>
        ) : null}

        {children}
      </div>
    </div>
  )
}

export function SectionCard({ title, description, icon: Icon, actions, children, className = '' }) {
  return (
    <section className={`rounded-[28px] border border-[#eadfce] bg-[#fffdf9] p-6 shadow-sm ${className}`}>
      {(title || description || actions) ? (
        <div className="mb-5 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div className="max-w-2xl">
            <div className="flex items-center gap-3">
              {Icon ? (
                <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#f7efe2] text-[#d67d1f]">
                  <Icon size={20} />
                </span>
              ) : null}
              <div>
                {title ? <h2 className="text-lg font-semibold text-[#1f2328]">{title}</h2> : null}
                {description ? <p className="mt-1 text-sm text-[#6a7077]">{description}</p> : null}
              </div>
            </div>
          </div>

          {actions ? <div className="flex flex-wrap gap-3">{actions}</div> : null}
        </div>
      ) : null}

      {children}
    </section>
  )
}

export function MetricCard({ label, value, icon: Icon, hint }) {
  return (
    <div className="rounded-[28px] border border-[#eadfce] bg-[#fffdf9] p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm text-[#6a7077]">{label}</p>
          <p className="mt-2 text-2xl font-black tracking-tight text-[#1f2328]">{value}</p>
          {hint ? <p className="mt-2 text-xs text-[#8b9198]">{hint}</p> : null}
        </div>
        {Icon ? (
          <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#f7efe2] text-[#d67d1f]">
            <Icon size={20} />
          </span>
        ) : null}
      </div>
    </div>
  )
}

export function StatusBanner({ message }) {
  if (!message) return null

  const classes = message.type === 'success'
    ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
    : 'border-red-200 bg-red-50 text-red-700'

  return (
    <p className={`rounded-2xl border px-4 py-3 text-sm ${classes}`}>
      {message.text}
    </p>
  )
}

export function EmptyPanel({ title, description, action }) {
  return (
    <div className="rounded-[28px] border border-dashed border-[#dbcdb9] bg-[#fffdf9] px-6 py-14 text-center shadow-sm">
      <h3 className="text-xl font-semibold text-[#1f2328]">{title}</h3>
      <p className="mx-auto mt-3 max-w-xl text-sm leading-7 text-[#6a7077]">{description}</p>
      {action ? <div className="mt-6">{action}</div> : null}
    </div>
  )
}

export function AppField({ label, value, onChange, type = 'text', placeholder, required = true, hint }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-medium text-[#30343a]">{label}</span>
      <input
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        className="w-full rounded-2xl border border-[#ded3c4] bg-white px-4 py-3 text-sm text-[#1f2328] outline-none transition focus:border-[#d67d1f] focus:ring-4 focus:ring-[#f4ddbf]"
      />
      {hint ? <span className="mt-1.5 block text-xs text-[#8b9198]">{hint}</span> : null}
    </label>
  )
}

export function AppTextarea({ label, value, onChange, rows = 4, placeholder, required = true, hint }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-sm font-medium text-[#30343a]">{label}</span>
      <textarea
        value={value}
        onChange={onChange}
        rows={rows}
        placeholder={placeholder}
        required={required}
        className="w-full rounded-2xl border border-[#ded3c4] bg-white px-4 py-3 text-sm text-[#1f2328] outline-none transition focus:border-[#d67d1f] focus:ring-4 focus:ring-[#f4ddbf]"
      />
      {hint ? <span className="mt-1.5 block text-xs text-[#8b9198]">{hint}</span> : null}
    </label>
  )
}
