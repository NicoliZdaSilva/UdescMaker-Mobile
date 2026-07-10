import { Platform, type ViewStyle } from 'react-native';

export const colors = {
  background: '#f8f5eb',
  backgroundGreen: '#d6f0d9',
  panel: '#dff4e3',
  panelStrong: '#a9d9b0',
  panelDeep: '#91c89a',
  brand: '#0f5c3b',
  brandDeep: '#0a3f29',
  brandSoft: '#91d19b',
  text: '#171717',
  muted: '#53625b',
  line: 'rgba(23, 23, 23, 0.22)',
  white: '#ffffff',
  danger: '#a52b2b',
  dangerSoft: '#fde8e8',
  warning: '#8a5a00',
  success: '#17653b',
  successSoft: '#e2f7e8',
  infoSoft: '#e8f2fb'
} as const;

export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  xxl: 32
} as const;

export const radii = {
  sm: 10,
  md: 16,
  lg: 24,
  pill: 999
} as const;

export const shadows: Record<'card' | 'floating', ViewStyle> = {
  card: Platform.select({
    ios: {
      shadowColor: colors.brandDeep,
      shadowOpacity: 0.12,
      shadowRadius: 12,
      shadowOffset: { width: 0, height: 5 }
    },
    android: { elevation: 3 },
    default: {}
  }) as ViewStyle,
  floating: Platform.select({
    ios: {
      shadowColor: colors.text,
      shadowOpacity: 0.16,
      shadowRadius: 8,
      shadowOffset: { width: 0, height: 3 }
    },
    android: { elevation: 5 },
    default: {}
  }) as ViewStyle
};
