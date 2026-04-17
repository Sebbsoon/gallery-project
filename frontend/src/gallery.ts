import type { GalleryImage } from './types'

export function visibleImages(images: GalleryImage[]): GalleryImage[] {
  return images.filter((image) => !image.hidden)
}

export function tagsFromImages(images: GalleryImage[]): string[] {
  const tags = images.flatMap((image) => image.tags ?? [])
  return [...new Set(tags)].sort((a, b) => a.localeCompare(b))
}

export function filterByTag(images: GalleryImage[], tag: string): GalleryImage[] {
  if (!tag) {
    return images
  }
  return images.filter((image) => image.tags.includes(tag))
}
