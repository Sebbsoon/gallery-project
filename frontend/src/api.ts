import type {
  GalleryImage,
  ImageMetadataPayload,
  ImageTagsPayload,
  ImageVisibilityPayload,
} from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.trim() || 'http://localhost:8080'

function buildUrl(path: string): string {
  return `${API_BASE_URL}${path}`
}

function looksLikeHtml(value: string): boolean {
  const normalized = value.trim().toLowerCase()
  return normalized.startsWith('<!doctype html') || normalized.startsWith('<html')
}

function toUserErrorMessage(response: Response, rawBody: string, contentType: string | null): string {
  if (contentType?.includes('application/json')) {
    try {
      const payload = JSON.parse(rawBody) as { message?: string; error?: string }
      if (payload.message?.trim()) return payload.message
      if (payload.error?.trim()) return payload.error
    } catch {
      // Fallback below
    }
  }

  if (contentType?.includes('text/html') || looksLikeHtml(rawBody)) {
    if (response.status === 404) {
      return 'Could not reach the API endpoint (404). Verify VITE_API_BASE_URL points to the backend.'
    }
    return `Unexpected HTML response from API (${response.status}). Verify backend URL configuration.`
  }

  const text = rawBody.trim()
  if (text.length > 0) {
    return text
  }

  return `Request failed: ${response.status} ${response.statusText}`
}

async function apiRequest<T>(path: string, init?: RequestInit, token?: string): Promise<T> {
  const headers = new Headers(init?.headers)
  if (!headers.has('Content-Type') && init?.body) {
    headers.set('Content-Type', 'application/json')
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  let response: Response
  try {
    response = await fetch(buildUrl(path), { ...init, headers })
  } catch {
    throw new Error('Could not connect to the API. Check backend availability and CORS settings.')
  }

  if (!response.ok) {
    const rawBody = await response.text()
    throw new Error(toUserErrorMessage(response, rawBody, response.headers.get('Content-Type')))
  }

  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

export function fetchImages(): Promise<GalleryImage[]> {
  return apiRequest<GalleryImage[]>('/api/images')
}

export function uploadLocalImages(token: string): Promise<void> {
  return apiRequest<void>('/api/images/upload', { method: 'POST' }, token)
}

export function updateImageMetadata(
  id: number,
  payload: ImageMetadataPayload,
  token: string,
): Promise<GalleryImage> {
  return apiRequest<GalleryImage>(
    `/api/images/${id}`,
    {
      method: 'PUT',
      body: JSON.stringify(payload),
    },
    token,
  )
}

export function updateImageVisibility(
  id: number,
  payload: ImageVisibilityPayload,
  token: string,
): Promise<GalleryImage> {
  return apiRequest<GalleryImage>(
    `/api/images/${id}/visibility`,
    {
      method: 'PATCH',
      body: JSON.stringify(payload),
    },
    token,
  )
}

export function deleteImage(id: number, token: string): Promise<void> {
  return apiRequest<void>(`/api/images/${id}`, { method: 'DELETE' }, token)
}

export function addImageTags(id: number, payload: ImageTagsPayload, token: string): Promise<GalleryImage> {
  return apiRequest<GalleryImage>(
    `/api/images/${id}/tags`,
    {
      method: 'POST',
      body: JSON.stringify(payload),
    },
    token,
  )
}

export function removeImageTag(id: number, tagName: string, token: string): Promise<GalleryImage> {
  return apiRequest<GalleryImage>(
    `/api/images/${id}/tags/${encodeURIComponent(tagName)}`,
    {
      method: 'DELETE',
    },
    token,
  )
}
