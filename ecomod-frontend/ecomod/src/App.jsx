import { BrowserRouter, Navigate, Routes, Route } from 'react-router-dom'
import { CartProvider } from './context/CartContext'
import Navbar from './components/Navbar'
import Home from './pages/Home'
import Catalog from './pages/Catalog'
import ProductDetail from './pages/ProductDetail'
import Cart from './pages/Cart'
import Inventory from './pages/Inventory'
import Account from './pages/Account'
import BuyerAccount from './pages/BuyerAccount'
import SellerOnboarding from './pages/SellerOnboarding'
import { SecureRoutes } from './context/SecureRoutes'
import VerifyEmail from './components/VerifyEmail'
import Orders from './pages/Orders'
import Payment from './pages/Payment'

function App() {
  return (
    <CartProvider>
      <BrowserRouter>
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/catalog" element={<Catalog />} />
          <Route path="/product/:id" element={<ProductDetail />} />
          <Route path="/cart" element={<Cart />} />
          <Route path="/account" element={<Account />} />
          <Route path="/verify-email" element={<VerifyEmail />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/payment" element={<Payment />} />
          <Route path="/me" element={
            <SecureRoutes requireCustomer={true}>
              <BuyerAccount />
            </SecureRoutes>
          } />
          <Route path="/sell" element={
            <SecureRoutes requireCustomer={true} authIntent="seller">
              <SellerOnboarding />
            </SecureRoutes>
          } />
          <Route path="/seller" element={
            <SecureRoutes requireBusiness={true} authIntent="seller">
              <Inventory />
            </SecureRoutes>
          } />
          <Route path="/inventory" element={<Navigate to="/seller" replace />} />
        </Routes>
        <img
          src="https://freedns.afraid.org/images/powerani.gif"
          alt="Powered by FreeDNS"
          style={{
            position: 'fixed',
            bottom: '16px',
            right: '16px',
            zIndex: 9999,
          }}
        />
      </BrowserRouter>
    </CartProvider>
  )
}

export default App
