import { useEffect, useState } from 'react';
import { ScrollView, StyleSheet, Text } from 'react-native';

import { AsyncState } from '../components/AsyncState';
import { BrandHeader } from '../components/BrandHeader';
import { Button } from '../components/Button';
import { ChoiceChips } from '../components/ChoiceChips';
import { FormField } from '../components/FormField';
import { Screen } from '../components/Screen';
import { useTaxonomy } from '../hooks/useProjects';
import type { ScreenProps } from '../navigation/types';
import { colors, spacing } from '../theme';
import type { DifficultyId, ProjectFilters, TaxonomyOption } from '../types/api';
import { applyDraftFilters, createEmptyFilters } from '../utils/filters';

const ageOptions = [3, 6, 10, 12, 14, 16].map((age) => ({ id: String(age), label: `${age}+` }));
const durationOptions: TaxonomyOption[] = [
  { id: '30', label: 'Até 30 min' },
  { id: '60', label: 'Até 1 h' },
  { id: '120', label: 'Até 2 h' },
  { id: '240', label: 'Até 4 h' }
];

export function ExploreScreen({ navigation, route }: ScreenProps<'Explore'>) {
  const taxonomy = useTaxonomy();
  const [filters, setFilters] = useState<ProjectFilters>(
    route.params?.initialFilters ?? createEmptyFilters()
  );
  const [tagText, setTagText] = useState(filters.tags?.join(', ') ?? '');

  useEffect(() => {
    if (route.params?.initialFilters) {
      setFilters(route.params.initialFilters);
      setTagText(route.params.initialFilters.tags?.join(', ') ?? '');
    }
  }, [route.params?.initialFilters]);

  function applyFilters() {
    navigation.navigate('Results', { filters: applyDraftFilters(filters, tagText) });
  }

  function clearFilters() {
    setFilters(createEmptyFilters());
    setTagText('');
  }

  return (
    <Screen keyboard>
      <BrandHeader onBack={navigation.goBack} />
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <Text accessibilityRole="header" style={styles.title}>
          Explorar projetos
        </Text>
        <AsyncState error={taxonomy.error} loading={taxonomy.loading} onRetry={taxonomy.reload}>
          <FormField
            label="Buscar projeto"
            onChangeText={(busca) => setFilters((current) => ({ ...current, busca }))}
            placeholder="Busque por título, tags ou conteúdo"
            returnKeyType="search"
            value={filters.busca ?? ''}
          />
          <FormField
            label="Tags ou palavras-chave"
            onChangeText={setTagText}
            placeholder="Separe por vírgulas"
            value={tagText}
          />
          <ChoiceChips
            label="Idade da pessoa que fará o projeto"
            onChange={(values) =>
              setFilters((current) => ({ ...current, idade: values[0] ? Number(values[0]) : undefined }))
            }
            options={ageOptions}
            values={filters.idade == null ? [] : [String(filters.idade)]}
          />
          <ChoiceChips
            label="Duração máxima"
            onChange={(values) =>
              setFilters((current) => ({
                ...current,
                duracaoMaxima: values[0] ? Number(values[0]) : undefined
              }))
            }
            options={durationOptions}
            values={filters.duracaoMaxima == null ? [] : [String(filters.duracaoMaxima)]}
          />
          <ChoiceChips
            label="Categoria"
            onChange={(values) => setFilters((current) => ({ ...current, categoria: values[0] }))}
            options={taxonomy.data?.categorias ?? []}
            values={filters.categoria ? [filters.categoria] : []}
          />
          <ChoiceChips
            label="Nível de dificuldade"
            onChange={(values) =>
              setFilters((current) => ({ ...current, dificuldade: values[0] as DifficultyId | undefined }))
            }
            options={taxonomy.data?.dificuldades ?? []}
            values={filters.dificuldade ? [filters.dificuldade] : []}
          />
          <Button label="Aplicar filtros" onPress={applyFilters} testID="apply-filters" />
          <Button label="Limpar filtros" onPress={clearFilters} testID="clear-filters" variant="secondary" />
        </AsyncState>
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.lg, paddingBottom: spacing.xxl, gap: spacing.lg },
  title: {
    color: colors.text,
    fontSize: 22,
    fontWeight: '900',
    textAlign: 'center',
    textTransform: 'uppercase',
    marginBottom: spacing.sm
  }
});
