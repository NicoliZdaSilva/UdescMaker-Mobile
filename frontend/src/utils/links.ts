import { Linking } from 'react-native';

export function isSafePublicUrl(value: string) {
  try {
    const url = new URL(value);
    return url.protocol === 'https:' || url.protocol === 'http:';
  } catch {
    return false;
  }
}

export async function openSafePublicUrl(value: string) {
  if (!isSafePublicUrl(value)) throw new Error('O endereço deste arquivo não é válido.');
  await Linking.openURL(value);
}
