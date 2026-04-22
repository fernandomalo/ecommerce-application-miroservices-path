import { useCallback, useEffect, useRef, useState } from 'react'
import { useSelector } from 'react-redux'
import { getCompanyByUserId, getUserInfo } from '../services/api'
import { usePrivateAxios } from './usePrivateAxios'

function normalizeRecord(value) {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    return null
  }

  return value
}

export function useAccountData() {
  const privateApi = usePrivateAxios()
  const { token, userId } = useSelector((state) => state.auth)
  const [userInfo, setUserInfo] = useState(null)
  const [company, setCompany] = useState(null)
  const [loading, setLoading] = useState(false)
  const userInfoRef = useRef(userInfo)
  const companyRef = useRef(company)

  useEffect(() => {
    userInfoRef.current = userInfo
  }, [userInfo])

  useEffect(() => {
    companyRef.current = company
  }, [company])

  const reload = useCallback(async () => {
    if (!token || !userId) {
      setUserInfo(null)
      setCompany(null)
      return { userInfo: null, company: null }
    }

    setLoading(true)

    try {
      const [userResult, companyResult] = await Promise.allSettled([
        getUserInfo(userId, privateApi),
        getCompanyByUserId(userId, privateApi),
      ])

      const nextUser = userResult.status === 'fulfilled'
        ? normalizeRecord(userResult.value)
        : userInfoRef.current
      const nextCompany = companyResult.status === 'fulfilled'
        ? normalizeRecord(companyResult.value)
        : companyRef.current

      setUserInfo(nextUser)
      setCompany(nextCompany)

      console.log("USER FROM API:", userResult)

      return { userInfo: nextUser, company: nextCompany }
    } finally {
      setLoading(false)
    }
  }, [privateApi, token, userId])

  useEffect(() => {
    reload().catch(() => {})
  }, [reload])

  return { userInfo, company, loading, reload, setUserInfo, setCompany }
}
