declare module 'browser-image-compression' {
  interface Options {
    maxSizeMB?: number;
    maxWidthOrHeight?: number;
    useWebWorker?: boolean;
    maxIteration?: number;
    exifOrientation?: number;
    onProgress?: (progress: number) => void;
    fileType?: string;
    initialQuality?: number;
    alwaysKeepResolution?: boolean;
    preserveExif?: boolean;
  }

  function imageCompression(file: File, options?: Options): Promise<File>;

  export default imageCompression;
}
