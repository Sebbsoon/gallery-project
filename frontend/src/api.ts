import type {
  GalleryImage,
  ImageMetadataPayload,
  ImageTagsPayload,
  ImageVisibilityPayload,
} from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

function buildUrl(path: string): string {
  return `${API_BASE_URL}${path}`
}

async function apiRequest<T>(path: string, init?: RequestInit, token?: string): Promise<T> {
  const headers = new Headers(init?.headers)
  if (!headers.has('Content-Type') && init?.body) {
    headers.set('Content-Type', 'application/json')
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(buildUrl(path), { ...init, headers })
  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || `${response.status} ${response.statusText}`)
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
