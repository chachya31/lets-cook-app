import { useCallback, useRef, useState } from 'react'
import { ImagePlus, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { cn } from '@/lib/utils'

interface ImageUploaderProps {
  value: File | string | null
  onChange: (file: File | null) => void
  className?: string
}

export const ImageUploader = ({ value, onChange, className }: ImageUploaderProps) => {
  const inputRef = useRef<HTMLInputElement>(null)
  const [isDragOver, setIsDragOver] = useState(false)

  const previewUrl = value
    ? typeof value === 'string'
      ? value
      : URL.createObjectURL(value)
    : null

  const handleFileSelect = useCallback(
    (file: File | null) => {
      if (file && !file.type.startsWith('image/')) {
        alert('画像ファイルを選択してください')
        return
      }
      onChange(file)
    },
    [onChange]
  )

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] || null
    handleFileSelect(file)
  }

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(true)
  }

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragOver(false)
    const file = e.dataTransfer.files?.[0] || null
    handleFileSelect(file)
  }

  const handleRemove = () => {
    onChange(null)
    if (inputRef.current) {
      inputRef.current.value = ''
    }
  }

  const handleClick = () => {
    inputRef.current?.click()
  }

  return (
    <div className={cn('space-y-2', className)}>
      <input
        ref={inputRef}
        type="file"
        accept="image/*"
        onChange={handleInputChange}
        className="hidden"
      />

      {previewUrl ? (
        <div className="relative">
          <img
            src={previewUrl}
            alt="プレビュー"
            className="h-48 w-full rounded-md object-cover"
          />
          <Button
            type="button"
            variant="destructive"
            size="icon"
            onClick={handleRemove}
            className="absolute right-2 top-2 h-8 w-8"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      ) : (
        <div
          onClick={handleClick}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          className={cn(
            'flex h-48 w-full cursor-pointer items-center justify-center rounded-md border-2 border-dashed transition-colors',
            isDragOver
              ? 'border-emerald-500 bg-emerald-50'
              : 'border-muted-foreground/25 hover:border-muted-foreground/50'
          )}
        >
          <div className="flex flex-col items-center gap-2 text-muted-foreground">
            <ImagePlus className="h-10 w-10" />
            <span className="text-sm">クリックまたはドラッグで画像をアップロード</span>
            <span className="text-xs">JPEG, PNG, GIF, WebP</span>
          </div>
        </div>
      )}
    </div>
  )
}
