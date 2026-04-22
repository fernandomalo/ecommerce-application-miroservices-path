import axios from 'axios'

export const api = axios.create({
  withCredentials: true,
})

export const shippingApi = axios.create({
  baseURL: '/shipping-api',
})

const unwrap = (response) => response.data

export const registerUser = (payload) => api.post('/auth/register', payload).then(unwrap)
export const loginUser = (payload) => api.post('/auth/login', payload).then(unwrap)
export const logoutUser = () => api.post('/auth/logout').then(unwrap)

export const getProducts = (client = api) =>
  client.get('/api/v1/products/see/list').then(unwrap)

export const getProduct = (id, client = api) =>
  client.get(`/api/v1/products/see/${id}`).then(unwrap)

export const getProductAggregate = (id, client = api) =>
  client.get(`/api/v1/products/${id}`).then(unwrap)

export const createProduct = (payload, client = api) =>
  client.post('/api/v1/products/create', payload).then(unwrap)

export const updateProduct = (productId, payload, client = api) =>
  client.patch(`/api/v1/products/update/${productId}`, payload).then(unwrap)

export const getCategories = (client = api) =>
  client.get('/api/v1/categories/list').then(unwrap)

export const createCategory = (payload, client = api) =>
  client.post('/api/v1/categories/add-new', payload).then(unwrap)

export const updateCategory = (id, payload, client = api) =>
  client.put(`/api/v1/categories/update/${id}`, payload).then(unwrap)

export const getStock = (productId, client = api) =>
  client.get(`/api/v1/inventory/products/${productId}`).then(unwrap)

export const updateStock = (productId, quantity, type, client = api) =>
  client.put(`/api/v1/inventory/products/update/${productId}`, null, {
    params: { quantity, type },
  }).then(unwrap)

export const getUserInfo = (userId, client = api) =>
  client.get(`/api/v1/users/info`).then(unwrap)

export const saveUserInfo = (payload, client = api) =>
  client.post('/api/v1/users/add-info', payload).then(unwrap)

export const getCompanyByUserId = (userId, client = api) =>
  client.get(`/api/v1/companies/user`).then(unwrap)

export const getCompanyBySlug = (slug, client = api) =>
  client.get(`/api/v1/companies/slug/${slug}`).then(unwrap)

export const createCompany = (payload, client = api) =>
  client.post('/api/v1/companies/create-company', payload).then(unwrap)

export const verifyUser = (code) =>
  api.post(`/auth/verify?code=${code}`).then((r) => r.data)

export const resendVerificationCode = (email) =>
  api.post(`/auth/resend-code?email=${encodeURIComponent(email)}`).then((r) => r.data)

export async function getShippingRule(companyId, privateApi) {
  const response = await privateApi.get('/api/v1/users/shipping-price', {
    params: { companyId },
  })
  return response.data
}

// Cart
export const fetchCartByUser = (client = api) =>
  client.get('/api/v1/cart/auth-user').then(unwrap)

export const fetchCartAnonymous = (client = api) =>
  client.get('/api/v1/cart/anonymous').then(unwrap)

export const addItemToCart = (productId, ruleId, client = api) =>
  client.post(`/api/v1/cart/add-item/${productId}`, null, {
    params: { ruleId },
  }).then(unwrap)

export const removeItemFromCart = (cartId, productId, client = api) =>
  client.delete(`/api/v1/cart/remove-item/${cartId}/${productId}`).then(unwrap)

export const increaseItemQty = (cartId, productId, client = api) =>
  client.put(`/api/v1/cart/increase-quantity/${cartId}/${productId}`).then(unwrap)

export const decreaseItemQty = (cartId, productId, client = api) =>
  client.put(`/api/v1/cart/decrease-quantity/${cartId}/${productId}`).then(unwrap)

export const toggleItemStatus = (cartId, productId, client = api) =>
  client.put(`/api/v1/cart/toggle-status/${cartId}/${productId}`).then(unwrap)

export const mergeCart = (token) =>
  axios.post('/api/v1/cart/merge', null, {
    withCredentials: true,
    headers: { Authorization: `Bearer ${token}` },
  }).then(unwrap)

export const checkoutCart = (client = api) =>
  client.post('/api/v1/cart/checkout').then(unwrap)

export const getPaymentConfig = (client = api) =>
  client.get('/api/v1/payments/config').then(unwrap)

export const getPaymentByOrder = (orderId, client = api) =>
  client.get(`/api/v1/payments/order/${orderId}`).then(unwrap)

export const getPaymentByReference = (reference, client = api) =>
  client.get('/api/v1/payments/status', { params: { reference } }).then(unwrap)

export const initiatePayment = (orderId, payload, client = api) =>
  client.post('/api/v1/payments/initiate', payload, { params: { orderId } }).then(unwrap)

export const getPendingPayment = (client = api) =>
  client.get('/api/v1/payments/pending').then(unwrap)

export const getOrdersByUser = (client = api) =>
  client.get('/api/v1/orders/user').then(unwrap)

export const getOrderById = (id, client = api) =>
  client.get(`/api/v1/orders/${id}`).then(unwrap)

export const getShippingRuleById = (id) =>
  api.get(`/api/v1/shipping-rules/price/${id}`).then(unwrap)