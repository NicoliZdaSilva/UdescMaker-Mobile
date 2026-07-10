import type { PropsWithChildren } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { colors, radii, shadows, spacing } from '../theme';

interface ContentSectionProps extends PropsWithChildren {
  title: string;
  testID?: string;
}

export function ContentSection({ title, testID, children }: ContentSectionProps) {
  return (
    <View style={[styles.section, shadows.card]} testID={testID}>
      <Text accessibilityRole="header" style={styles.title}>
        {title}
      </Text>
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  section: {
    backgroundColor: 'rgba(255,255,255,0.76)',
    borderRadius: radii.lg,
    borderWidth: 1,
    borderColor: colors.line,
    padding: spacing.lg,
    gap: spacing.md
  },
  title: { color: colors.text, fontSize: 18, fontWeight: '900', textTransform: 'uppercase' }
});
