import { useState } from 'react';
import { RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';

import { AsyncState } from '../components/AsyncState';
import { BrandHeader } from '../components/BrandHeader';
import { Button } from '../components/Button';
import { ProjectCard } from '../components/ProjectCard';
import { Screen } from '../components/Screen';
import { SearchBar } from '../components/SearchBar';
import { useProjects } from '../hooks/useProjects';
import type { ScreenProps } from '../navigation/types';
import { colors, spacing } from '../theme';

export function HomeScreen({ navigation }: ScreenProps<'Home'>) {
  const [search, setSearch] = useState('');
  const projects = useProjects({ ordenacao: 'recentes', limite: 6 });

  function submitSearch() {
    const value = search.trim();
    if (value) navigation.navigate('Results', { filters: { busca: value, ordenacao: 'recentes' } });
    else navigation.navigate('Explore');
  }

  return (
    <Screen>
      <ScrollView
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
        refreshControl={<RefreshControl onRefresh={projects.reload} refreshing={projects.refreshing} />}
      >
        <BrandHeader />
        <View style={styles.hero}>
          <Text style={styles.kicker}>FAÇA • APRENDA • COMPARTILHE</Text>
          <Text accessibilityRole="header" style={styles.title}>
            Ideias maker para colocar a mão na massa
          </Text>
          <SearchBar onChangeText={setSearch} onSubmit={submitSearch} value={search} />
          <View style={styles.actions}>
            <Button
              icon="plus-circle-outline"
              label="Postar Projeto"
              onPress={() => navigation.navigate('Publish')}
              style={styles.action}
              variant="secondary"
            />
            <Button
              icon="compass-outline"
              label="Explorar projetos"
              onPress={() => navigation.navigate('Explore')}
              style={styles.action}
            />
          </View>
        </View>

        <Text accessibilityRole="header" style={styles.sectionTitle}>
          Projetos recentes
        </Text>
        <AsyncState
          empty={projects.data?.length === 0}
          emptyActionLabel="Explorar projetos"
          emptyMessage="O catálogo ainda não possui projetos disponíveis."
          emptyTitle="Nenhum projeto publicado"
          error={projects.error}
          loading={projects.loading}
          onEmptyAction={() => navigation.navigate('Explore')}
          onRetry={projects.reload}
        >
          {projects.data?.map((project) => (
            <ProjectCard
              key={project.slug}
              onPress={() => navigation.navigate('Details', { slug: project.slug })}
              project={project}
            />
          ))}
        </AsyncState>
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  content: { paddingBottom: spacing.xxl, paddingHorizontal: spacing.lg },
  hero: { gap: spacing.lg, marginBottom: spacing.xxl },
  kicker: { color: colors.brand, fontSize: 12, fontWeight: '900', letterSpacing: 1.2 },
  title: { color: colors.text, fontSize: 28, lineHeight: 33, fontWeight: '900' },
  actions: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.md },
  action: { flexGrow: 1, minWidth: 150 },
  sectionTitle: {
    color: colors.text,
    textAlign: 'center',
    textTransform: 'uppercase',
    fontWeight: '900',
    fontSize: 20,
    marginBottom: spacing.lg
  }
});
