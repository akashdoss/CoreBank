import React, { useState, useEffect } from 'react'

function App() {
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [isTxModalOpen, setIsTxModalOpen] = useState(false)
  const [isReportOpen, setIsReportOpen] = useState(false)
  const [reportData, setReportData] = useState(null)

  const [txForm, setTxForm] = useState({
    type: 'TRANSFER',
    source: '',
    target: '',
    amount: ''
  })

  const fetchData = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/accounts')
      const data = await res.json()
      setAccounts(data)
      setLoading(false)
    } catch (err) {
      console.error('Failed to fetch accounts:', err)
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchData()
  }, [])

  const handleTransaction = async (e) => {
    e.preventDefault()
    try {
      const res = await fetch('http://localhost:8080/api/transactions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(txForm)
      })
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.error)
      }
      alert('Transaction Successful!')
      setIsTxModalOpen(false)
      fetchData()
    } catch (err) {
      alert('Error: ' + err.message)
    }
  }

  const openReports = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/analytics')
      const data = await res.json()
      setReportData(data)
      setIsReportOpen(true)
    } catch (err) {
      alert('Failed to load report')
    }
  }

  const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0)

  return (
    <div className="dashboard">
      <header className="header">
        <h1>CoreBank <span style={{ fontSize: '1rem', verticalAlign: 'middle', opacity: 0.6 }}>PRO</span></h1>
        <p style={{ color: '#94a3b8' }}>Professional Java-Driven Financial Ecosystem</p>
      </header>

      <div className="stats-grid">
        <div className="card">
          <h3>Total Assets</h3>
          <div className="value">${totalBalance.toLocaleString()}</div>
        </div>
        <div className="card">
          <h3>Active Accounts</h3>
          <div className="value">{accounts.length}</div>
        </div>
        <div className="card">
          <h3>System Status</h3>
          <div className="value" style={{ color: '#10b981', fontSize: '1.25rem' }}>OPERATIONAL</div>
        </div>
      </div>

      <div className="card table-card">
        <h3>Accounts Overview</h3>
        {loading ? (
          <p>Connecting to Java Backend...</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Account Number</th>
                <th>Type</th>
                <th>Balance</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map(acc => (
                <tr key={acc.accountNumber}>
                  <td>{acc.accountNumber}</td>
                  <td>{acc.type}</td>
                  <td style={{ fontWeight: 600 }}>${acc.balance.toLocaleString()}</td>
                  <td>
                    <span className={`status-badge status-${acc.status.toLowerCase()}`}>
                      {acc.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div style={{ marginTop: '2rem', display: 'flex', gap: '1rem' }}>
        <button className="btn" onClick={() => setIsTxModalOpen(true)}>New Transaction</button>
        <button className="btn" style={{ background: 'transparent', border: '1px solid var(--card-border)' }} onClick={openReports}>Reports</button>
      </div>

      {isTxModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2 style={{ marginTop: 0 }}>New Transaction</h2>
            <form onSubmit={handleTransaction}>
              <div className="form-group">
                <label>Type</label>
                <select value={txForm.type} onChange={e => setTxForm({ ...txForm, type: e.target.value })}>
                  <option value="TRANSFER">Transfer</option>
                  <option value="DEPOSIT">Deposit</option>
                  <option value="WITHDRAWAL">Withdrawal</option>
                </select>
              </div>
              <div className="form-group">
                <label>Source Account</label>
                <input value={txForm.source} onChange={e => setTxForm({ ...txForm, source: e.target.value })} placeholder="ACC..." required />
              </div>
              {txForm.type === 'TRANSFER' && (
                <div className="form-group">
                  <label>Target Account</label>
                  <input value={txForm.target} onChange={e => setTxForm({ ...txForm, target: e.target.value })} placeholder="ACC..." required />
                </div>
              )}
              <div className="form-group">
                <label>Amount ($)</label>
                <input type="number" value={txForm.amount} onChange={e => setTxForm({ ...txForm, amount: e.target.value })} placeholder="0.00" required />
              </div>
              <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
                <button type="submit" className="btn" style={{ flex: 1 }}>Confirm</button>
                <button type="button" className="btn" style={{ flex: 1, background: '#333' }} onClick={() => setIsTxModalOpen(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {isReportOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2 style={{ marginTop: 0 }}>Financial Report</h2>
            <div className="report-grid">
              <div className="report-item">
                <label style={{ display: 'block', fontSize: '0.75rem', opacity: 0.6 }}>TOTAL VOLUME</label>
                <div style={{ fontSize: '1.25rem', fontWeight: 600 }}>${reportData?.totalVolume.toLocaleString()}</div>
              </div>
              <div className="report-item">
                <label style={{ display: 'block', fontSize: '0.75rem', opacity: 0.6 }}>HIGH VALUE TXs</label>
                <div style={{ fontSize: '1.25rem', fontWeight: 600 }}>{reportData?.highValueCount}</div>
              </div>
            </div>
            <div className="report-item" style={{ marginTop: '1rem', background: 'rgba(16, 185, 129, 0.05)', borderColor: 'rgba(16, 185, 129, 0.2)' }}>
              <label style={{ display: 'block', fontSize: '0.75rem', opacity: 0.6, color: '#10b981' }}>ALGO STATUS</label>
              <div style={{ fontSize: '1rem', fontWeight: 600, color: '#10b981' }}>{reportData?.status}</div>
            </div>
            <button className="btn" style={{ marginTop: '2rem', width: '100%', background: '#333' }} onClick={() => setIsReportOpen(false)}>Close</button>
          </div>
        </div>
      )}
    </div>
  )
}

export default App
