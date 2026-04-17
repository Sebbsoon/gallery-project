import { describe, expect, it } from 'vitest'
import { filterByTag, tagsFromImages, visibleImages } from './gallery'
import type { GalleryImage } from './types'

const images: GalleryImage[] = [
  {
    id: 1,
    fileName: 'a.jpg',
    title: 'A',
    description: null,
    bucketPath: null,
    hidden: false,
    createdAt: null,
    updatedAt: null,
    url: null,
    tags: ['nature', 'travel'],
  },
  {
    id: 2,
    fileName: 'b.jpg',
    title: 'B',
    description: null,
    bucketPath: null,
    hidden: true,
    createdAt: null,
    updatedAt: null,
    url: null,
    tags: ['portrait'],
  },
]

describe('gallery helpers', () => {
  it('filters hidden images for guest view', () => {
    expect(visibleImages(images)).toHaveLength(1)
    expect(visibleImages(images)[0].id).toBe(1)
  })

  it('collects unique sorted tags', () => {
    expect(tagsFromImages(images)).toEqual(['nature', 'portrait', 'travel'])
  })

  it('filters by selected tag', () => {
    expect(filterByTag(images, 'portrait')).toHaveLength(1)
    expect(filterByTag(images, 'unknown')).toHaveLength(0)
  })
})
