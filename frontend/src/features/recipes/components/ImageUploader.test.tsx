import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ImageUploader } from './ImageUploader'

// window.alertをモック
const mockAlert = vi.fn()
global.alert = mockAlert

describe('ImageUploader', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('初期表示', () => {
    it('画像が未選択の場合、アップロードエリアが表示されること', () => {
      const onChange = vi.fn()
      render(<ImageUploader value={null} onChange={onChange} />)

      expect(screen.getByText('クリックまたはドラッグで画像をアップロード')).toBeInTheDocument()
      expect(screen.getByText('JPEG, PNG, GIF, WebP')).toBeInTheDocument()
    })

    it('既存のURL（文字列）が渡された場合、プレビュー画像が表示されること', () => {
      const onChange = vi.fn()
      render(<ImageUploader value="https://example.com/image.jpg" onChange={onChange} />)

      const img = screen.getByRole('img', { name: 'プレビュー' })
      expect(img).toBeInTheDocument()
      expect(img).toHaveAttribute('src', 'https://example.com/image.jpg')
    })

    it('Fileオブジェクトが渡された場合、プレビュー画像が表示されること', () => {
      const onChange = vi.fn()
      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      render(<ImageUploader value={file} onChange={onChange} />)

      const img = screen.getByRole('img', { name: 'プレビュー' })
      expect(img).toBeInTheDocument()
      expect(URL.createObjectURL).toHaveBeenCalledWith(file)
    })
  })

  describe('ファイル選択', () => {
    it('ファイルを選択した際、onChangeハンドラが正しく呼ばれること', async () => {
      const user = userEvent.setup()
      const onChange = vi.fn()
      render(<ImageUploader value={null} onChange={onChange} />)

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const input = document.querySelector('input[type="file"]') as HTMLInputElement

      await user.upload(input, file)

      expect(onChange).toHaveBeenCalledTimes(1)
      expect(onChange).toHaveBeenCalledWith(file)
    })

    it('画像以外のファイルを選択した場合、アラートが表示されonChangeが呼ばれないこと', () => {
      const onChange = vi.fn()
      render(<ImageUploader value={null} onChange={onChange} />)

      const file = new File(['test'], 'test.txt', { type: 'text/plain' })
      const input = document.querySelector('input[type="file"]') as HTMLInputElement

      // fireEventを使用してaccept属性をバイパス
      fireEvent.change(input, { target: { files: [file] } })

      expect(mockAlert).toHaveBeenCalledWith('画像ファイルを選択してください')
      expect(onChange).not.toHaveBeenCalled()
    })

    it('アップロードエリアをクリックするとファイル選択ダイアログが開くこと', async () => {
      const user = userEvent.setup()
      const onChange = vi.fn()
      render(<ImageUploader value={null} onChange={onChange} />)

      const input = document.querySelector('input[type="file"]') as HTMLInputElement
      const clickSpy = vi.spyOn(input, 'click')

      const uploadArea = screen.getByText('クリックまたはドラッグで画像をアップロード').parentElement?.parentElement
      if (uploadArea) {
        await user.click(uploadArea)
      }

      expect(clickSpy).toHaveBeenCalled()
    })
  })

  describe('ドラッグ&ドロップ', () => {
    it('ファイルをドロップした際、onChangeハンドラが正しく呼ばれること', () => {
      const onChange = vi.fn()
      render(<ImageUploader value={null} onChange={onChange} />)

      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      const uploadArea = screen.getByText('クリックまたはドラッグで画像をアップロード').parentElement?.parentElement

      if (uploadArea) {
        fireEvent.drop(uploadArea, {
          dataTransfer: {
            files: [file],
          },
        })
      }

      expect(onChange).toHaveBeenCalledTimes(1)
      expect(onChange).toHaveBeenCalledWith(file)
    })

    it('ドラッグオーバー時にスタイルが変わること', () => {
      const onChange = vi.fn()
      render(<ImageUploader value={null} onChange={onChange} />)

      const uploadArea = screen.getByText('クリックまたはドラッグで画像をアップロード').parentElement?.parentElement

      if (uploadArea) {
        fireEvent.dragOver(uploadArea)
        expect(uploadArea).toHaveClass('border-emerald-500')

        fireEvent.dragLeave(uploadArea)
        expect(uploadArea).not.toHaveClass('border-emerald-500')
      }
    })
  })

  describe('削除', () => {
    it('削除ボタンを押すと、onChangeがnullで呼ばれること', async () => {
      const user = userEvent.setup()
      const onChange = vi.fn()
      render(<ImageUploader value="https://example.com/image.jpg" onChange={onChange} />)

      const deleteButton = screen.getByRole('button')
      await user.click(deleteButton)

      expect(onChange).toHaveBeenCalledTimes(1)
      expect(onChange).toHaveBeenCalledWith(null)
    })

    it('Fileオブジェクトのプレビュー時に削除ボタンを押すと、値がクリアされること', async () => {
      const user = userEvent.setup()
      const onChange = vi.fn()
      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' })
      render(<ImageUploader value={file} onChange={onChange} />)

      const deleteButton = screen.getByRole('button')
      await user.click(deleteButton)

      expect(onChange).toHaveBeenCalledTimes(1)
      expect(onChange).toHaveBeenCalledWith(null)
    })
  })

  describe('アクセシビリティ', () => {
    it('input要素にaccept属性が設定されていること', () => {
      const onChange = vi.fn()
      render(<ImageUploader value={null} onChange={onChange} />)

      const input = document.querySelector('input[type="file"]') as HTMLInputElement
      expect(input).toHaveAttribute('accept', 'image/*')
    })

    it('input要素が非表示になっていること', () => {
      const onChange = vi.fn()
      render(<ImageUploader value={null} onChange={onChange} />)

      const input = document.querySelector('input[type="file"]') as HTMLInputElement
      expect(input).toHaveClass('hidden')
    })
  })
})
