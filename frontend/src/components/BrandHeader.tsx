import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, spacing } from '../theme';

interface BrandHeaderProps {
  onBack?: () => void;
  compact?: boolean;
}

export function BrandHeader({ onBack, compact = false }: BrandHeaderProps) {
  return (
    <View style={[styles.header, compact && styles.headerCompact]}>
      <View style={styles.side}>
        {onBack ? (
          <Pressable
            accessibilityLabel="Voltar"
            accessibilityRole="button"
            hitSlop={12}
            onPress={onBack}
            style={({ pressed }) => [styles.back, pressed && styles.pressed]}
          >
            <MaterialCommunityIcons color={colors.text} name="arrow-left" size={26} />
          </Pressable>
        ) : null}
      </View>
      <View accessibilityLabel="UDESC MAKER" accessibilityRole="header" style={styles.brand}>
        <View style={styles.mark}>
          <MaterialCommunityIcons color={colors.white} name="wrench" size={26} />
        </View>
        <View>
          <Text style={styles.udesc}>UDESC</Text>
          <Text style={styles.maker}>MAKER</Text>
        </View>
      </View>
      <View style={styles.side} />
    </View>
  );
}

const styles = StyleSheet.create({
  header: {
    minHeight: 84,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between'
  },
  headerCompact: { minHeight: 68, paddingVertical: spacing.sm },
  side: { width: 42 },
  back: { width: 42, height: 42, alignItems: 'center', justifyContent: 'center' },
  pressed: { opacity: 0.58, transform: [{ scale: 0.97 }] },
  brand: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  mark: {
    width: 45,
    height: 45,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.brand
  },
  udesc: { color: '#b43030', fontSize: 19, lineHeight: 19, fontWeight: '900' },
  maker: { color: colors.text, fontSize: 19, lineHeight: 19, fontWeight: '900' }
});
