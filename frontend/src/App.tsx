import { useEffect, useMemo, useState } from 'react'
import './App.css'
import type { GalleryImage } from './types'
import { filterByTag, tagsFromImages, visibleImages } from './gallery'
import {
  addImageTags,
  deleteImage,
  fetchImages,
  removeImageTag,
  updateImageMetadata,
  updateImageVisibility,
  uploadLocalImages,
} from './api'

export type AuthContext = {
  isSignedIn: boolean
  getToken?: () => Promise<string | null>
  userLabel?: string
}

type AppProps = {
  auth: AuthContext
}

function App({ auth }: AppProps) {
  const [images, setImages] = useState<GalleryImage[]>([])
  const [selectedTag, setSelectedTag] = useState('')
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [tagInputs, setTagInputs] = useState<Record<number, string>>({})

  const guestVisible = useMemo(() => visibleImages(images), [images])
  const filterTags = useMemo(() => tagsFromImages(guestVisible), [guestVisible])
  const guestFiltered = useMemo(
    () => filterByTag(guestVisible, selectedTag),
    [guestVisible, selectedTag],
  )

  async function refreshImages(): Promise<void> {
    try {
      setLoading(true)
      setError(null)
      setImages(await fetchImages())
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load images')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    let cancelled = false

    void fetchImages()
      .then((data) => {
        if (!cancelled) {
          setImages(data)
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'Failed to load images')
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  async function requireToken(): Promise<string> {
    if (!auth.getToken) {
      throw new Error('Sign in is required for admin actions')
    }
    const token = await auth.getToken()
    if (!token) {
      throw new Error('Could not retrieve Clerk token')
    }
    return token
  }

  async function runAdminAction(action: () => Promise<void>): Promise<void> {
    try {
      setBusy(true)
      setError(null)
      await action()
      await refreshImages()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Action failed')
    } finally {
      setBusy(false)
    }
  }

  async function handleUploadLocalImages(): Promise<void> {
    await runAdminAction(async () => {
      const token = await requireToken()
      await uploadLocalImages(token)
    })
  }

  async function handleUpdateMetadata(image: GalleryImage): Promise<void> {
    const nextTitle = window.prompt('Title', image.title ?? '')
    if (nextTitle === null) return
    const nextDescription = window.prompt('Description', image.description ?? '')
    if (nextDescription === null) return

    await runAdminAction(async () => {
      const token = await requireToken()
      await updateImageMetadata(
        image.id,
        {
          title: nextTitle.trim(),
          description: nextDescription,
        },
        token,
      )
    })
  }

  async function handleToggleVisibility(image: GalleryImage): Promise<void> {
    await runAdminAction(async () => {
      const token = await requireToken()
      await updateImageVisibility(image.id, { hidden: !image.hidden }, token)
    })
  }

  async function handleDeleteImage(image: GalleryImage): Promise<void> {
    if (!window.confirm(`Delete image "${image.title ?? image.fileName ?? image.id}"?`)) {
      return
    }

    await runAdminAction(async () => {
      const token = await requireToken()
      await deleteImage(image.id, token)
    })
  }

  async function handleAddTag(image: GalleryImage): Promise<void> {
    const raw = (tagInputs[image.id] ?? '').trim()
    if (!raw) return

    const tags = raw
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)

    if (tags.length === 0) return

    await runAdminAction(async () => {
      const token = await requireToken()
      await addImageTags(image.id, { tags }, token)
      setTagInputs((prev) => ({ ...prev, [image.id]: '' }))
    })
  }

  async function handleRemoveTag(image: GalleryImage, tagName: string): Promise<void> {
    await runAdminAction(async () => {
      const token = await requireToken()
      await removeImageTag(image.id, tagName, token)
    })
  }

  return (
    <main className="app">
      <header className="topbar">
        <div>
          <h1>Gallery</h1>
          <p className="muted">
            {auth.isSignedIn
              ? `Signed in as ${auth.userLabel ?? 'admin'}`
              : 'Guest mode: only visible images are shown'}
          </p>
        </div>
      </header>

      {error && <p className="error">{error}</p>}

      <section className="panel">
        <h2>Guest view</h2>
        <label>
          Filter by tag:
          <select
            value={selectedTag}
            onChange={(event) => setSelectedTag(event.target.value)}
            disabled={loading}
          >
            <option value="">All tags</option>
            {filterTags.map((tag) => (
              <option key={tag} value={tag}>
                {tag}
              </option>
            ))}
          </select>
        </label>

        {loading ? (
          <p>Loading images...</p>
        ) : (
          <div className="grid">
            {guestFiltered.map((image) => (
              <article key={image.id} className="card">
                {image.url ? <img src={image.url} alt={image.title ?? 'Gallery image'} /> : null}
                <h3>{image.title ?? image.fileName ?? `Image ${image.id}`}</h3>
                <p>{image.description ?? 'No description'}</p>
                <p className="muted">Tags: {image.tags.length > 0 ? image.tags.join(', ') : 'none'}</p>
              </article>
            ))}
            {guestFiltered.length === 0 && <p>No images found for this filter.</p>}
          </div>
        )}
      </section>

      {auth.isSignedIn && (
        <section className="panel">
          <h2>Admin controls</h2>
          <p className="muted">
            Requires admin permissions from Clerk token claims recognized by the backend.
          </p>
          <button type="button" onClick={() => void handleUploadLocalImages()} disabled={busy}>
            Trigger local upload batch
          </button>

          <div className="admin-list">
            {images.map((image) => (
              <article key={image.id} className="admin-item">
                <div className="admin-item-head">
                  <strong>{image.title ?? image.fileName ?? `Image ${image.id}`}</strong>
                  <span className="muted">#{image.id}</span>
                </div>
                <p className="muted">Visible: {image.hidden ? 'No (hidden)' : 'Yes'}</p>
                <p className="muted">Tags: {image.tags.length > 0 ? image.tags.join(', ') : 'none'}</p>
                <div className="actions">
                  <button type="button" onClick={() => void handleUpdateMetadata(image)} disabled={busy}>
                    Edit metadata
                  </button>
                  <button type="button" onClick={() => void handleToggleVisibility(image)} disabled={busy}>
                    {image.hidden ? 'Unhide' : 'Hide'}
                  </button>
                  <button type="button" onClick={() => void handleDeleteImage(image)} disabled={busy}>
                    Delete
                  </button>
                </div>
                <div className="tag-actions">
                  <input
                    type="text"
                    placeholder="tag1, tag2"
                    value={tagInputs[image.id] ?? ''}
                    onChange={(event) =>
                      setTagInputs((prev) => ({ ...prev, [image.id]: event.target.value }))
                    }
                    disabled={busy}
                  />
                  <button type="button" onClick={() => void handleAddTag(image)} disabled={busy}>
                    Add tags
                  </button>
                </div>
                {image.tags.length > 0 && (
                  <div className="tag-chip-list">
                    {image.tags.map((tag) => (
                      <button
                        key={`${image.id}-${tag}`}
                        type="button"
                        className="tag-chip"
                        onClick={() => void handleRemoveTag(image, tag)}
                        disabled={busy}
                      >
                        Remove {tag}
                      </button>
                    ))}
                  </div>
                )}
              </article>
            ))}
          </div>
        </section>
      )}
    </main>
  )
}

export default App
