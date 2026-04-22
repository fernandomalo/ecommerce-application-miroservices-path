import { useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import { useNavigate } from 'react-router-dom'
import { LayoutGrid } from 'lucide-react'

// Frontend-defined parent categories with icons and color backgrounds
// Subcategories map to your backend category names
const CATEGORY_TREE = [
    {
        label: 'Tecnología',
        icon: '💻',
        subs: ['Technology', 'Photography', 'Music', 'Games', 'Office Supplies', 'Accessories'],
    },
    {
        label: 'Salud & Bienestar',
        icon: '💪',
        subs: ['Health', 'Fitness', 'Supplements', 'Sports', 'Beauty', 'Watches'],
    },
    {
        label: 'Hogar & Jardín',
        icon: '🏠',
        subs: ['Home', 'Garden', 'Tools', 'Office Supplies', 'Outdoor & Camping', 'Pets'],
    },
    {
        label: 'Arte & Ocio',
        icon: '🎨',
        subs: ['Art & Craft', 'Books', 'Distraction', 'Collectibles', 'Toys', 'Music'],
    },
    {
        label: 'Aventura',
        icon: '🏕️',
        subs: ['Outdoor & Camping', 'Sports', 'Travel', 'Fitness', 'Automotive', 'Photography'],
    },
    {
        label: 'Mascotas & Más',
        icon: '🐾',
        subs: ['Pets', 'Toys', 'Collectibles', 'Garden', 'Art & Craft', 'Accessories'],
    },
]

// Soft background colors for sub-item icons
const BG_CYCLE = [
    '#FFF3E0', '#E8F5E9', '#E3F2FD', '#FFFDE7',
    '#FCE4EC', '#F3E5F5', '#E8EAF6', '#FBE9E7',
]

// Map sub-label → emoji icon (extend as needed)
const SUB_ICONS = {
    'Technology': '📱',
    'Photography': '📷',
    'Music': '🎵',
    'Games': '🎮',
    'Office Supplies': '📎',
    'Accessories': '🕶️',
    'Health': '❤️',
    'Fitness': '🏋️',
    'Supplements': '💊',
    'Sports': '⚽',
    'Beauty': '💄',
    'Watches': '⌚',
    'Home': '🛋️',
    'Garden': '🌿',
    'Tools': '🔧',
    'Outdoor & Camping': '⛺',
    'Pets': '🐾',
    'Art & Craft': '🎨',
    'Books': '📚',
    'Distraction': '🎯',
    'Collectibles': '🏆',
    'Toys': '🧸',
    'Travel': '✈️',
    'Automotive': '🚗',
}

export default function CategoryMenu({ backendCategories = [] }) {
    const navigate = useNavigate()
    const [open, setOpen] = useState(false)
    const [activeIndex, setActiveIndex] = useState(0)
    const [panelPos, setPanelPos] = useState({ top: 0, left: 0 })
    const btnRef = useRef(null)
    const panelRef = useRef(null)

    // Close on outside click
    useEffect(() => {
        if (open && btnRef.current) {
            const rect = btnRef.current.getBoundingClientRect()
            setPanelPos({
                top: rect.bottom + window.scrollY + 8,
                left: rect.left + window.scrollX,
            })
        }
    }, [open])

    // Close on outside click
    useEffect(() => {
        const handler = (e) => {
            if (
                panelRef.current && !panelRef.current.contains(e.target) &&
                btnRef.current && !btnRef.current.contains(e.target)
            ) {
                setOpen(false)
            }
        }
        document.addEventListener('mousedown', handler)
        return () => document.removeEventListener('mousedown', handler)
    }, [])

    // Close on scroll (panel position would drift)
    useEffect(() => {
        if (!open) return
        const handler = () => setOpen(false)
        window.addEventListener('scroll', handler, { passive: true })
        return () => window.removeEventListener('scroll', handler)
    }, [open])

    const active = CATEGORY_TREE[activeIndex]

    const matchedSubs = active.subs.map((subLabel) => {
        const matched = backendCategories.find(
            (bc) => bc.name.toLowerCase() === subLabel.toLowerCase()
        )
        return { label: subLabel, backendName: matched?.name ?? subLabel }
    })

    const panel = open ? createPortal(
        <div
            ref={panelRef}
            style={{
                position: 'absolute',
                top: panelPos.top,
                left: panelPos.left,
                zIndex: 9999,
                width: 1245,
                height: 500
            }}
            className="flex overflow-hidden rounded-2xl border border-[#eadfce] bg-white shadow-xl"
        >
            {/* Sidebar */}
            <div className="w-48 shrink-0 border-r border-[#eadfce]">
                {CATEGORY_TREE.map((cat, i) => (
                    <button
                        key={cat.label}
                        type="button"
                        onMouseEnter={() => setActiveIndex(i)}
                        onClick={() => setActiveIndex(i)}
                        className={`flex w-full items-center gap-2.5 border-l-[3px] px-4 py-2.5 text-left text-sm transition ${activeIndex === i
                                ? 'border-[#d67d1f] bg-[#fdf6ee] font-semibold text-[#1f2328]'
                                : 'border-transparent text-[#5f6368] hover:bg-[#fdf6ee] hover:text-[#1f2328]'
                            }`}
                    >
                        <span style={{ fontSize: 16 }}>{cat.icon}</span>
                        {cat.label}
                    </button>
                ))}
            </div>

            {/* Main grid */}
            <div className="flex-1 p-5">
                <p className="mb-3 text-xs font-semibold uppercase tracking-widest text-[#8b9198]">
                    {active.label}
                </p>
                <div className="grid grid-cols-4 gap-3">
                    {matchedSubs.map((sub, i) => (
                        <button
                            key={sub.label}
                            type="button"
                            onClick={() => {
                                navigate(`/catalog?category=${encodeURIComponent(sub.backendName)}`)
                                setOpen(false)
                            }}
                            className="flex flex-col items-center gap-2 rounded-xl p-3 text-center transition hover:bg-[#f7f1e8]"
                        >
                            <div
                                className="flex h-14 w-14 items-center justify-center rounded-xl text-2xl"
                                style={{ background: BG_CYCLE[i % BG_CYCLE.length] }}
                            >
                                {SUB_ICONS[sub.label] ?? '📦'}
                            </div>
                            <span className="text-[11px] leading-tight text-[#5f6368]">{sub.label}</span>
                        </button>
                    ))}
                </div>
            </div>
        </div>,
        document.body   // ← renders outside navbar, no clipping
    ) : null

    return (
        <>
            <button
                ref={btnRef}
                type="button"
                onClick={() => setOpen((o) => !o)}
                onMouseEnter={() => setOpen(true)}
                className="flex shrink-0 items-center gap-2 rounded-full bg-[#1f2328] px-4 py-2 text-sm font-semibold text-white transition hover:bg-[#2b3037]"
            >
                <LayoutGrid size={14} />
                Todas las categorías
            </button>

            {panel}
        </>
    )
}