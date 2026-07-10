import { useMemo, useState } from 'react';
import { RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';

import { AsyncState } from '../components/AsyncState';
import { BrandHeader } from '../components/BrandHeader';
import { Button } from '../components/Button';
import { ChoiceChips } from '../components/ChoiceChips';
import { ProjectCard } from '../components/ProjectCard';
import { Screen } from '../components/Screen';
import { SearchBar } from '../components/SearchBar';
import { useProjects } from '../hooks/useProjects';
import type { ScreenProps } from '../navigation/types';
import { colors, radii, spacing } from '../theme';
import type { ProjectFilters, ProjectOrder, TaxonomyOption } from '../types/api';

const orderOptions: TaxonomyOption<ProjectOrder>[] = [
  { id: 'recentes', label: 'Mais recentes' },
  { id: 'duracao', label: 'Mais rápidos' },
  { id: 'dificuldade', label: 'Mais fáceis' }
];

function describeFilters(filters: ProjectFilters) {
  const parts = [
    filters.tags?.length ? `Tags: ${filters.tags.join(', ')}` : '',
    filters.categoria ? `Categoria: ${filters.categoria}` : '',
    filters.idade != null ? `Idade: ${filters.idade}+` : '',
    filters.duracaoMaxima ? `Duração: até ${filters.duracaoMaxima} min` : '',
    filters.dificuldade ? `Nível: ${filters.dificuldade}` : ''
  ].filter(Boolean);
  return parts.length ? parts.join(' • ') : 'Todos os projetos';
}

export function ResultsScreen({ navigation, route }: ScreenProps<'Results'>) {
  const [filters, setFilters] = useState<ProjectFilters>(route.params.filters);
  const [search, setSearch] = useState(filters.busca ?? '');
  const appliedFilters = useMemo(() => ({ ...filters, busca: search.trim() || undefined }), [filters, search]);
  const projects = useProjects(appliedFilters);

  function setOrder(order: string[]) {
    setFilters((current) => ({ ...current, ordenacao: order[0] as ProjectOrder }));
  }

  function clearAll() {
    setSearch('');
    setFilters({ ordenacao: filters.ordenacao ?? 'recentes' });
  }

  return (
    <Screen>
      <BrandHeader onBack={navigation.goBack} />
      <ScrollView
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
        refreshControl={<RefreshControl onRefresh={projects.reload} refreshing={projects.refreshing} />}
      >
        <SearchBar onChangeText={setSearch} onSubmit={projects.reload} value={search} />
        <View style={styles.summaryBox}>
          <Text style={styles.filterSummary}>{describeFilters(filters)}</Text>
          <Button
            label="Alterar filtros"
            onPress={() => navigation.navigate('Explore', { initialFilters: filters })}
            variant="secondary"
          />
        </View>
        <ChoiceChips
          label="Ordenar por"
          onChange={setOrder}
          options={orderOptions}
          values={[filters.ordenacao ?? 'recentes']}
        />
        <View style={styles.resultHeader}>
          <Text accessibilityRole="header" style={styles.title}>
            Resultados
          </Text>
          <Text accessibilityLiveRegion="polite" style={styles.count}>
            {projects.data?.length ?? 0} projeto{projects.data?.length === 1 ? '' : 's'}
          </Text>
        </View>
        <AsyncState
          empty={projects.data?.length === 0}
          emptyActionLabel="Limpar filtros"
          error={projects.error}
          loading={projects.loading}
          onEmptyAction={clearAll}
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
  content: { paddingHorizontal: spacing.lg, paddingBottom: spacing.xxl, gap: spacing.lg },
  summaryBox: {
    gap: spacing.md,
    padding: spacing.md,
    borderWidth: 1,
    borderColor: colors.line,
    borderRadius: radii.md,
    backgroundColor: 'rgba(255,255,255,0.55)'
  },
  filterSummary: { color: colors.muted, lineHeight: 20 },
  resultHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  title: { color: colors.text, fontWeight: '900', fontSize: 21, textTransform: 'uppercase' },
  count: { color: colors.muted, fontWeight: '700' }
});
