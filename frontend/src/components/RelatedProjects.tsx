import { StyleSheet, Text, View } from 'react-native';

import { colors, spacing } from '../theme';
import type { ProjectSummary } from '../types/api';
import { ProjectCard } from './ProjectCard';

interface RelatedProjectsProps {
  projects: ProjectSummary[];
  onOpen: (slug: string) => void;
}

export function RelatedProjects({ projects, onOpen }: RelatedProjectsProps) {
  if (projects.length === 0) return null;

  return (
    <View style={styles.root} testID="related-projects">
      <Text accessibilityRole="header" style={styles.title}>
        Projetos relacionados
      </Text>
      {projects.map((project) => (
        <ProjectCard compact key={project.slug} onPress={() => onOpen(project.slug)} project={project} />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  root: { gap: spacing.sm },
  title: { color: colors.text, fontSize: 20, fontWeight: '900', textTransform: 'uppercase' }
});
