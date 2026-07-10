import { StyleSheet, Text, View } from 'react-native';

import { colors, spacing } from '../theme';

interface MarkdownTextProps {
  markdown: string;
}

function cleanInlineMarkdown(value: string) {
  return value
    .replace(/!\[([^\]]*)]\([^)]*\)/g, '$1')
    .replace(/\[([^\]]+)]\((?:https?:\/\/)[^)]*\)/g, '$1')
    .replace(/[*_`~]/g, '')
    .trim();
}

export function MarkdownText({ markdown }: MarkdownTextProps) {
  const lines = markdown.split(/\r?\n/);
  return (
    <View style={styles.root}>
      {lines.map((raw, index) => {
        const line = raw.trim();
        if (!line) return <View key={index} style={styles.space} />;

        const heading = line.match(/^(#{1,3})\s+(.+)$/);
        if (heading) {
          return (
            <Text key={index} style={heading[1]?.length === 1 ? styles.h1 : styles.h2}>
              {cleanInlineMarkdown(heading[2] ?? '')}
            </Text>
          );
        }

        const bullet = line.match(/^[-*+]\s+(.+)$/);
        if (bullet) {
          return (
            <View key={index} style={styles.bulletRow}>
              <Text style={styles.bullet}>•</Text>
              <Text style={styles.paragraph}>{cleanInlineMarkdown(bullet[1] ?? '')}</Text>
            </View>
          );
        }

        return (
          <Text key={index} style={styles.paragraph}>
            {cleanInlineMarkdown(line)}
          </Text>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  root: { gap: spacing.sm },
  space: { height: spacing.xs },
  h1: { color: colors.text, fontWeight: '900', fontSize: 21, marginTop: spacing.sm },
  h2: { color: colors.text, fontWeight: '800', fontSize: 17, marginTop: spacing.sm },
  paragraph: { color: colors.muted, fontSize: 15, lineHeight: 23, flexShrink: 1 },
  bulletRow: { flexDirection: 'row', gap: spacing.sm, paddingLeft: spacing.sm },
  bullet: { color: colors.brand, fontWeight: '900', fontSize: 16 }
});
