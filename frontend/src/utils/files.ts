import * as DocumentPicker from 'expo-document-picker';
import * as ImagePicker from 'expo-image-picker';

import type { SelectedFile } from '../types/forms';

function fallbackName(uri: string, prefix: string) {
  const lastPart = decodeURIComponent(uri.split('/').pop() ?? '').split('?')[0];
  return lastPart || `${prefix}-${Date.now()}`;
}

export async function pickImage(): Promise<SelectedFile | null> {
  const result = await ImagePicker.launchImageLibraryAsync({
    mediaTypes: ['images'],
    allowsEditing: false,
    quality: 0.9
  });

  if (result.canceled) return null;
  const asset = result.assets[0];
  if (!asset) return null;

  return {
    uri: asset.uri,
    name: asset.fileName ?? fallbackName(asset.uri, 'imagem.jpg'),
    mimeType: asset.mimeType ?? 'image/jpeg',
    size: asset.fileSize,
    webFile: asset.file
  };
}

export async function pickDocument(): Promise<SelectedFile | null> {
  const result = await DocumentPicker.getDocumentAsync({
    copyToCacheDirectory: true,
    multiple: false
  });

  if (result.canceled) return null;
  const asset = result.assets[0];
  if (!asset) return null;

  return {
    uri: asset.uri,
    name: asset.name ?? fallbackName(asset.uri, 'arquivo'),
    mimeType: asset.mimeType ?? 'application/octet-stream',
    size: asset.size,
    webFile: asset.file
  };
}
