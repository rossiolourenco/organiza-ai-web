const host = typeof window !== 'undefined' ? window.location.hostname : 'localhost';
const local = host === 'localhost' || host === '127.0.0.1' || host === '::1';

/** Em produção usa a mesma origem (`/api`) protegida pelo Nginx; no desenvolvimento local usa 8080. */
export const API_BASE = local ? 'http://localhost:8080/api' : '/api';
