import { ActivityIndicator, Pressable, StyleSheet, Text, type ViewStyle } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';

import { colors, radii, shadows, spacing } from '../theme';

interface ButtonProps {
  label: string;
  onPress: () => void;
  variant?: 'primary' | 'secondary' | 'danger';
  icon?: keyof typeof MaterialCommunityIcons.glyphMap;
  disabled?: boolean;
  loading?: boolean;
  style?: ViewStyle;
  testID?: string;
}

export function Button({
  label,
  onPress,
  variant = 'primary',
  icon,
  disabled,
  loading,
  style,
  testID
}: ButtonProps) {
  const blocked = disabled || loading;
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ disabled: blocked, busy: loading }}
      disabled={blocked}
      onPress={onPress}
      style={({ pressed }) => [
        styles.base,
        styles[variant],
        shadows.card,
        style,
        pressed && !blocked && styles.pressed,
        blocked && styles.disabled
      ]}
      testID={testID}
    >
      {loading ? (
        <ActivityIndicator color={variant === 'secondary' ? colors.brand : colors.white} />
      ) : icon ? (
        <MaterialCommunityIcons
          color={variant === 'secondary' ? colors.brandDeep : colors.white}
          name={icon}
          size={19}
        />
      ) : null}
      <Text style={[styles.label, variant === 'secondary' && styles.secondaryLabel]}>{label}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: {
    minHeight: 48,
    borderRadius: radii.pill,
    paddingHorizontal: spacing.lg,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: spacing.sm,
    borderWidth: 1
  },
  primary: { backgroundColor: colors.brandDeep, borderColor: colors.brandDeep },
  secondary: { backgroundColor: colors.white, borderColor: colors.brandDeep },
  danger: { backgroundColor: colors.danger, borderColor: colors.danger },
  label: { color: colors.white, fontWeight: '800', fontSize: 15 },
  secondaryLabel: { color: colors.brandDeep },
  pressed: { opacity: 0.82, transform: [{ scale: 0.985 }] },
  disabled: { opacity: 0.52 }
});
