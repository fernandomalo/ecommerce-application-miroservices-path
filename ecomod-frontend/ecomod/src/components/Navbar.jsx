import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useSelector } from 'react-redux'
import {
  LayoutDashboard, Search, ShoppingBag,
  ShoppingCart, Store, UserRound,
} from 'lucide-react'
import { getCategories } from '../services/api'
import { useCart } from '../context/CartContext'
import { isBusinessRole } from '../utils/authRole'
import CategoryMenu from './CategoryMenu'          // ← new import

export default function Navbar() {
  const navigate = useNavigate()
  const { items } = useCart()
  const { token, roles } = useSelector((state) => state.auth)
  const isBusiness = isBusinessRole(roles)
  const [search, setSearch] = useState('')
  const [categories, setCategories] = useState([])

  useEffect(() => {
    getCategories().then(setCategories).catch(() => setCategories([]))
  }, [])

  const totalItems = useMemo(
    () => (items ?? []).reduce((sum, item) => sum + Number(item.quantity ?? 0), 0),
    [items],
  )

  const handleSearch = (e) => {
    e.preventDefault()
    const query = search.trim()
    navigate(query ? `/catalog?q=${encodeURIComponent(query)}` : '/catalog')
  }

  return (
    <header className="sticky top-0 z-50 border-b border-[#eadfce] bg-[#fffdf9]/95 backdrop-blur">
      <div className="mx-auto flex max-w-7xl flex-col gap-3 px-4 py-4">

        {/* Top row — logo + search + nav */}
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center">
          <Link to="/" className="flex shrink-0 items-center gap-3">
            <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[#1f2328] text-white shadow-sm">
              <ShoppingBag size={20} />
            </span>
            <div>
              <p className="text-lg font-black tracking-tight text-[#1f2328]">EcoMod</p>
              <p className="text-xs uppercase tracking-[0.22em] text-[#b67a2c]">Marketplace</p>
            </div>
          </Link>

          <form className="flex flex-1 items-center" onSubmit={handleSearch}>
            <div className="flex w-full items-center overflow-hidden rounded-full border border-[#ded3c4] bg-white shadow-sm">
              <Search size={18} className="ml-4 text-[#8b9198]" />
              <input
                type="text"
                placeholder={isBusiness ? 'Busca productos del marketplace' : 'Buscar productos o categorias'}
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full px-3 py-3 text-sm outline-none"
              />
              <button type="submit" className="m-1 rounded-full bg-[#1f2328] px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-[#2b3037]">
                Buscar
              </button>
            </div>
          </form>

          <nav className="flex flex-wrap items-center gap-2 text-sm">
            <Link to="/catalog" className="inline-flex items-center gap-2 rounded-full px-4 py-2.5 font-medium text-[#5f6368] transition hover:bg-[#f3ede3] hover:text-[#1f2328]">
              <ShoppingBag size={16} /> Explorar
            </Link>
            {isBusiness ? (
              <Link to="/seller" className="inline-flex items-center gap-2 rounded-full px-4 py-2.5 font-medium text-[#5f6368] transition hover:bg-[#f3ede3] hover:text-[#1f2328]">
                <LayoutDashboard size={16} /> Mi tienda
              </Link>
            ) : (
              <Link to={token ? '/sell' : '/account?intent=seller'} className="inline-flex items-center gap-2 rounded-full px-4 py-2.5 font-medium text-[#5f6368] transition hover:bg-[#f3ede3] hover:text-[#1f2328]">
                <Store size={16} /> Vender
              </Link>
            )}
            <Link
              to={token ? (isBusiness ? '/seller' : '/me') : '/account'}
              className="inline-flex items-center gap-2 rounded-full px-4 py-2.5 font-medium text-[#5f6368] transition hover:bg-[#f3ede3] hover:text-[#1f2328]"
            >
              {isBusiness ? <LayoutDashboard size={16} /> : <UserRound size={16} />}
              {token ? (isBusiness ? 'Panel' : 'Cuenta') : 'Entrar'}
            </Link>
            {!isBusiness && (
              <Link to="/cart" className="relative inline-flex items-center gap-2 rounded-full px-4 py-2.5 font-medium text-[#5f6368] transition hover:bg-[#f3ede3] hover:text-[#1f2328]">
                <ShoppingCart size={16} /> Carrito
                {totalItems > 0 && (
                  <span className="absolute -right-1 -top-1 flex h-5 min-w-5 items-center justify-center rounded-full bg-[#d67d1f] px-1 text-[11px] font-bold text-white">
                    {totalItems}
                  </span>
                )}
              </Link>
            )}
          </nav>
        </div>

        {/* Bottom row — category menu + quick pills */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 text-sm text-[#6a7077]">
          <CategoryMenu backendCategories={categories} />   {/* ← replaces the old strip */}
          {categories.slice(0, 6).map((category) => (      /* show a few quick-access pills */
            <Link
              key={category.id}
              to={`/catalog?category=${encodeURIComponent(category.name)}`}
              className="shrink-0 rounded-full bg-white px-4 py-2 font-medium transition hover:bg-[#f3ede3]"
            >
              {category.name}
            </Link>
          ))}
        </div>

      </div>
    </header>
  )
}