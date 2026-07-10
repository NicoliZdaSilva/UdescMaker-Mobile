import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Image } from 'expo-image';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, radii, shadows, spacing } from '../theme';
import type { ProjectSummary } from '../types/api';
import { difficultyLabel, formatDate, formatDuration } from '../utils/format';

interface ProjectCardProps {
  project: ProjectSummary;
  onPress: () => void;
  compact?: boolean;
}

export function ProjectCard({ project, onPress, compact = false }: ProjectCardProps) {
  return (
    <Pressable
      accessibilityHint="Abre os detalhes do projeto"
      accessibilityLabel={`Projeto ${project.titulo}`}
      accessibilityRole="button"
      onPress={onPress}
      style={({ pressed }) => [
        styles.card,
        shadows.card,
        compact && styles.compact,
        pressed && styles.pressed
      ]}
      testID="project-card"
    >
      {project.capa.src ? (
        <Image
          accessibilityLabel={project.capa.alt}
          contentFit="cover"
          source={project.capa.src}
          style={[styles.image, compact && styles.imageCompact]}
          transition={180}
        />
      ) : (
        <View style={[styles.image, styles.placeholder, compact && styles.imageCompact]}>
          <MaterialCommunityIcons color={colors.brand} name="image-outline" size={42} />
        </View>
      )}
      <View style={styles.body}>
        <View style={styles.chipRow}>
          {project.categorias[0] ? (
            <Text style={styles.category}>{project.categorias[0]}</Text>
          ) : null}
          <Text style={styles.difficulty}>{difficultyLabel(project.dificuldade)}</Text>
        </View>
        <Text numberOfLines={2} style={styles.title}>
          {project.titulo}
        </Text>
        <Text numberOfLines={compact ? 2 : 3} style={styles.summary}>
          {project.resumo}
        </Text>
        <View style={styles.metaRow}>
          <Text style={styles.meta}>{formatDuration(project.duracaoMinutos)}</Text>
          <Text style={styles.meta}>{project.idadeMinima}+</Text>
          <Text style={styles.meta}>{formatDate(project.publicadoEm)}</Text>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    overflow: 'hidden',
    borderRadius: radii.lg,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.panelStrong,
    marginBottom: spacing.lg
  },
  compact: { marginBottom: spacing.md },
  pressed: { opacity: 0.82, transform: [{ scale: 0.992 }] },
  image: { width: '100%', height: 185, backgroundColor: colors.white },
  imageCompact: { height: 140 },
  placeholder: { alignItems: 'center', justifyContent: 'center' },
  body: { padding: spacing.lg, gap: spacing.sm },
  chipRow: { flexDirection: 'row', gap: spacing.sm, alignItems: 'center' },
  category: {
    color: colors.white,
    backgroundColor: colors.brand,
    borderRadius: radii.pill,
    paddingHorizontal: spacing.md,
    paddingVertical: 4,
    fontSize: 12,
    overflow: 'hidden',
    textTransform: 'capitalize'
  },
  difficulty: { color: colors.brandDeep, fontSize: 12, fontWeight: '700' },
  title: { color: colors.text, fontWeight: '900', fontSize: 18 },
  summary: { color: colors.muted, lineHeight: 20, fontSize: 14 },
  metaRow: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.md },
  meta: { color: colors.brandDeep, fontSize: 12, fontWeight: '700' }
});
