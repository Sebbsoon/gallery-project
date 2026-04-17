export type GalleryImage = {
  id: number
  fileName: string | null
  title: string | null
  description: string | null
  bucketPath: string | null
  hidden: boolean
  createdAt: string | null
  updatedAt: string | null
  url: string | null
  tags: string[]
}

export type ImageMetadataPayload = {
  title: string
  description: string
}

export type ImageVisibilityPayload = {
  hidden: boolean
}

export type ImageTagsPayload = {
  tags: string[]
}
