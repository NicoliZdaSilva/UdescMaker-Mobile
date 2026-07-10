import type { PropsWithChildren } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';

import { colors, radii, shadows, spacing } from '../theme';

interface AccordionSectionProps extends PropsWithChildren {
  title: string;
  expanded: boolean;
  onToggle: () => void;
}

export function AccordionSection({ title, expanded, onToggle, children }: AccordionSectionProps) {
  return (
    <View style={[styles.section, shadows.card]}>
      <Pressable
        accessibilityRole="button"
        accessibilityState={{ expanded }}
        onPress={onToggle}
        style={({ pressed }) => [styles.header, pressed && styles.pressed]}
      >
        <Text style={styles.title}>{title}</Text>
        <MaterialCommunityIcons
          color={colors.text}
          name={expanded ? 'chevron-up' : 'chevron-down'}
          size={24}
        />
      </Pressable>
      {expanded ? <View style={styles.content}>{children}</View> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  section: {
    borderRadius: radii.lg,
    backgroundColor: 'rgba(223,244,227,0.86)',
    borderWidth: 1,
    borderColor: colors.line,
    overflow: 'hidden'
  },
  header: {
    minHeight: 54,
    paddingHorizontal: spacing.lg,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between'
  },
  pressed: { opacity: 0.64 },
  title: { color: colors.text, fontSize: 16, fontWeight: '900', textTransform: 'uppercase' },
  content: { padding: spacing.lg, paddingTop: spacing.sm, gap: spacing.lg }
});
