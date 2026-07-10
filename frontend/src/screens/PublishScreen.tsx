import AsyncStorage from '@react-native-async-storage/async-storage';
import { MaterialCommunityIcons } from '@expo/vector-icons';
import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useRef, useState } from 'react';
import {
  Alert,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View
} from 'react-native';
import {
  Controller,
  type FieldError,
  type FieldErrors,
  type FieldPath,
  type Resolver,
  useFieldArray,
  useForm
} from 'react-hook-form';

import { AccordionSection } from '../components/AccordionSection';
import { AsyncState } from '../components/AsyncState';
import { BrandHeader } from '../components/BrandHeader';
import { Button } from '../components/Button';
import { ChoiceChips } from '../components/ChoiceChips';
import { DynamicTextList } from '../components/DynamicTextList';
import { FilePickerField } from '../components/FilePickerField';
import { FormField } from '../components/FormField';
import { Screen } from '../components/Screen';
import { useCatalog } from '../hooks/CatalogContext';
import { useTaxonomy } from '../hooks/useProjects';
import type { ScreenProps } from '../navigation/types';
import { ApiClientError } from '../services/apiClient';
import { publishProject } from '../services/projectsApi';
import { colors, radii, shadows, spacing } from '../theme';
import type { PublicationResponse } from '../types/api';
import type { PublicationFormValues, SelectedFile } from '../types/forms';
import { pickDocument, pickImage } from '../utils/files';
import { openSafePublicUrl } from '../utils/links';
import { defaultPublicationValues, publicationSchema } from '../validation/publication';

const AUTHOR_STORAGE_KEY = '@udescmaker/author';

type SectionId = 'basics' | 'details' | 'execution' | 'files' | 'other';

function message(error?: FieldError | { message?: string }) {
  return typeof error?.message === 'string' ? error.message : undefined;
}

function ItemCard({
  title,
  onRemove,
  children
}: {
  title: string;
  onRemove: () => void;
  children: React.ReactNode;
}) {
  return (
    <View style={styles.itemCard}>
      <View style={styles.itemHeader}>
        <Text style={styles.itemTitle}>{title}</Text>
        <Pressable accessibilityLabel={`Remover ${title}`} hitSlop={8} onPress={onRemove}>
          <MaterialCommunityIcons color={colors.danger} name="delete-outline" size={23} />
        </Pressable>
      </View>
      {children}
    </View>
  );
}

export function PublishScreen({ navigation }: ScreenProps<'Publish'>) {
  const taxonomy = useTaxonomy();
  const { invalidateCatalog } = useCatalog();
  const [publishing, setPublishing] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);
  const [success, setSuccess] = useState<PublicationResponse | null>(null);
  const scrollRef = useRef<ScrollView>(null);
  const [expanded, setExpanded] = useState<Record<SectionId, boolean>>({
    basics: true,
    details: false,
    execution: false,
    files: false,
    other: false
  });

  const {
    control,
    handleSubmit,
    formState: { errors },
    getValues,
    reset,
    setError,
    setValue,
    watch
  } = useForm<PublicationFormValues>({
    defaultValues: defaultPublicationValues,
    resolver: zodResolver(publicationSchema) as Resolver<PublicationFormValues>,
    mode: 'onBlur'
  });

  const tags = useFieldArray({ control, name: 'tags' });
  const materials = useFieldArray({ control, name: 'materiais' });
  const tools = useFieldArray({ control, name: 'ferramentas' });
  const tips = useFieldArray({ control, name: 'dicas' });
  const steps = useFieldArray({ control, name: 'passos' });
  const gallery = useFieldArray({ control, name: 'galeria' });
  const downloads = useFieldArray({ control, name: 'baixaveis' });
  const files = useFieldArray({ control, name: 'arquivos' });
  const values = watch();

  useEffect(() => {
    AsyncStorage.getItem(AUTHOR_STORAGE_KEY)
      .then((stored) => {
        if (!stored || getValues('autorNome')) return;
        const author = JSON.parse(stored) as { nome?: string; github?: string };
        if (author.nome) setValue('autorNome', author.nome);
        if (author.github) setValue('autorGithub', author.github);
      })
      .catch(() => {
        // Falha de armazenamento local não impede a publicação.
      });
  }, [getValues, setValue]);

  function toggle(section: SectionId) {
    setExpanded((current) => ({ ...current, [section]: !current[section] }));
  }

  function revealErrorSummary() {
    setTimeout(() => scrollRef.current?.scrollTo({ y: 0, animated: true }), 0);
  }

  function expandSectionsForFields(fieldNames: string[]) {
    const roots = new Set(fieldNames.map((field) => field.split(/[.[\]]/).filter(Boolean)[0]));
    setExpanded((current) => ({
      basics:
        current.basics ||
        ['titulo', 'resumo', 'autorNome', 'autorGithub', 'categorias', 'dificuldade', 'idadeMinima'].some(
          (field) => roots.has(field)
        ),
      details:
        current.details ||
        ['descricaoLonga', 'materiais', 'ferramentas', 'dicas'].some((field) => roots.has(field)),
      execution:
        current.execution ||
        ['duracaoMinutos', 'videoYoutube', 'capa', 'capaAlt', 'passos', 'galeria'].some((field) =>
          roots.has(field)
        ),
      files: current.files || ['baixaveis', 'arquivos'].some((field) => roots.has(field)),
      other: current.other || roots.has('tags')
    }));
  }

  async function chooseImage(onSelected: (file: SelectedFile) => void) {
    try {
      const file = await pickImage();
      if (file) onSelected(file);
    } catch {
      Alert.alert('Imagem não selecionada', 'Não foi possível acessar a biblioteca de imagens.');
    }
  }

  async function chooseDocument(onSelected: (file: SelectedFile) => void) {
    try {
      const file = await pickDocument();
      if (file) onSelected(file);
    } catch {
      Alert.alert('Arquivo não selecionado', 'Não foi possível abrir o seletor de documentos.');
    }
  }

  function applyBackendFields(error: ApiClientError) {
    const normalizedPaths: string[] = [];
    Object.entries(error.fields).forEach(([field, fieldMessage]) => {
      const normalizedPath = field
        .replace(/^projeto\./, '')
        .replace(/^autor\.nome$/, 'autorNome')
        .replace(/^autor\.github$/, 'autorGithub');
      normalizedPaths.push(normalizedPath);
      setError(normalizedPath as FieldPath<PublicationFormValues>, {
        type: 'server',
        message: fieldMessage
      });
    });
    expandSectionsForFields(normalizedPaths);
  }

  const submit = handleSubmit(
    async (formValues) => {
      if (publishing) return;
      setPublishing(true);
      setServerError(null);
      setFormError(null);
      setSuccess(null);

      try {
        const response = await publishProject(formValues);
        void AsyncStorage.setItem(
          AUTHOR_STORAGE_KEY,
          JSON.stringify({ nome: formValues.autorNome.trim(), github: formValues.autorGithub.trim() })
        ).catch(() => {
          // A preferência local é conveniente, mas nunca pode transformar uma publicação concluída em falha.
        });
        invalidateCatalog();
        setSuccess(response);
      } catch (error) {
        if (error instanceof ApiClientError) {
          applyBackendFields(error);
          setServerError(error.message);
        } else {
          setServerError('Não foi possível publicar o projeto. Tente novamente.');
        }
        revealErrorSummary();
      } finally {
        setPublishing(false);
      }
    },
    (invalidFields: FieldErrors<PublicationFormValues>) => {
      setServerError(null);
      setFormError('Revise os campos destacados nas seções abertas antes de publicar.');
      expandSectionsForFields(Object.keys(invalidFields));
      revealErrorSummary();
    }
  );

  function cancel() {
    Alert.alert('Cancelar cadastro?', 'Os dados preenchidos serão descartados.', [
      { text: 'Continuar editando', style: 'cancel' },
      {
        text: 'Descartar',
        style: 'destructive',
        onPress: () => {
          reset(defaultPublicationValues);
          navigation.goBack();
        }
      }
    ]);
  }

  if (success) {
    return (
      <Screen>
        <BrandHeader onBack={() => navigation.navigate('Home')} />
        <ScrollView contentContainerStyle={styles.successContent}>
          <MaterialCommunityIcons color={colors.success} name="check-decagram" size={72} />
          <Text accessibilityLiveRegion="polite" accessibilityRole="header" style={styles.successTitle}>
            Projeto enviado com sucesso
          </Text>
          <Text style={styles.successSlug}>{success.slug}</Text>
          <Text style={styles.successMessage}>{success.mensagem}</Text>
          <Text style={styles.successMessage}>
            O GitHub Pages pode levar alguns instantes para refletir a publicação.
          </Text>
          <Button
            label="Ver detalhes no aplicativo"
            onPress={() => navigation.replace('Details', { slug: success.slug })}
          />
          <Button
            label="Abrir projeto no site"
            onPress={() => void openSafePublicUrl(success.urlProjeto)}
            variant="secondary"
          />
          <Button label="Voltar ao início" onPress={() => navigation.navigate('Home')} variant="secondary" />
        </ScrollView>
      </Screen>
    );
  }

  return (
    <Screen keyboard>
      <BrandHeader onBack={navigation.goBack} />
      <ScrollView
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
        ref={scrollRef}
      >
        <Text accessibilityRole="header" style={styles.title}>
          Cadastrar projeto
        </Text>
        <Text style={styles.requiredHint}>Campos marcados com * são obrigatórios.</Text>

        {formError || serverError ? (
          <View accessibilityLiveRegion="assertive" style={styles.serverError}>
            <MaterialCommunityIcons color={colors.danger} name="alert-circle-outline" size={24} />
            <Text style={styles.serverErrorText}>{serverError ?? formError}</Text>
          </View>
        ) : null}

        <AsyncState error={taxonomy.error} loading={taxonomy.loading} onRetry={taxonomy.reload}>
          <AccordionSection
            expanded={expanded.basics}
            onToggle={() => toggle('basics')}
            title="Informações básicas"
          >
            <Controller
              control={control}
              name="titulo"
              render={({ field }) => (
                <FormField
                  error={message(errors.titulo)}
                  label="Título"
                  onBlur={field.onBlur}
                  onChangeText={field.onChange}
                  required
                  value={field.value}
                />
              )}
            />
            <Controller
              control={control}
              name="resumo"
              render={({ field }) => (
                <FormField
                  counter={`${field.value.length}/180`}
                  error={message(errors.resumo)}
                  label="Descrição curta"
                  maxLength={180}
                  multiline
                  onBlur={field.onBlur}
                  onChangeText={field.onChange}
                  required
                  value={field.value}
                />
              )}
            />
            <Controller
              control={control}
              name="autorNome"
              render={({ field }) => (
                <FormField
                  autoCapitalize="words"
                  error={message(errors.autorNome)}
                  label="Nome da pessoa autora"
                  onBlur={field.onBlur}
                  onChangeText={field.onChange}
                  required
                  value={field.value}
                />
              )}
            />
            <Controller
              control={control}
              name="autorGithub"
              render={({ field }) => (
                <FormField
                  autoCapitalize="none"
                  error={message(errors.autorGithub)}
                  label="Usuário do GitHub"
                  onBlur={field.onBlur}
                  onChangeText={field.onChange}
                  placeholder="opcional"
                  value={field.value}
                />
              )}
            />
            <ChoiceChips
              error={message(errors.categorias)}
              label="Categorias do projeto"
              multiple
              onChange={(selected) => setValue('categorias', selected, { shouldValidate: true })}
              options={taxonomy.data?.categorias ?? []}
              required
              values={values.categorias}
            />
            <ChoiceChips
              error={message(errors.dificuldade)}
              label="Nível de dificuldade"
              onChange={(selected) =>
                setValue('dificuldade', (selected[0] ?? '') as PublicationFormValues['dificuldade'], {
                  shouldValidate: true
                })
              }
              options={taxonomy.data?.dificuldades ?? []}
              required
              values={values.dificuldade ? [values.dificuldade] : []}
            />
            <Controller
              control={control}
              name="idadeMinima"
              render={({ field }) => (
                <FormField
                  error={message(errors.idadeMinima)}
                  keyboardType="number-pad"
                  label="Idade mínima"
                  onBlur={field.onBlur}
                  onChangeText={field.onChange}
                  required
                  value={field.value}
                />
              )}
            />
          </AccordionSection>

          <AccordionSection
            expanded={expanded.details}
            onToggle={() => toggle('details')}
            title="Detalhes do projeto"
          >
            <Controller
              control={control}
              name="descricaoLonga"
              render={({ field }) => (
                <FormField
                  error={message(errors.descricaoLonga)}
                  label="Sobre o projeto (obrigatório; Markdown simples)"
                  multiline
                  onBlur={field.onBlur}
                  onChangeText={field.onChange}
                  value={field.value}
                />
              )}
            />
            <DynamicTextList
              label="Materiais"
              onAdd={() => materials.append({ valor: '' })}
              onChange={(index, value) => setValue(`materiais.${index}.valor`, value)}
              onRemove={materials.remove}
              values={materials.fields.map((field, index) => ({ ...field, valor: values.materiais[index]?.valor ?? '' }))}
            />
            <DynamicTextList
              label="Ferramentas"
              onAdd={() => tools.append({ valor: '' })}
              onChange={(index, value) => setValue(`ferramentas.${index}.valor`, value)}
              onRemove={tools.remove}
              values={tools.fields.map((field, index) => ({ ...field, valor: values.ferramentas[index]?.valor ?? '' }))}
            />
            {tips.fields.map((field, index) => (
              <ItemCard key={field.id} onRemove={() => tips.remove(index)} title={`Dica ${index + 1}`}>
                <ChoiceChips
                  label="Tom"
                  onChange={(selected) =>
                    setValue(`dicas.${index}.tom`, (selected[0] ?? 'info') as 'info' | 'warning' | 'success')
                  }
                  options={taxonomy.data?.tonsDica ?? []}
                  values={[values.dicas[index]?.tom ?? 'info']}
                />
                <Controller
                  control={control}
                  name={`dicas.${index}.texto`}
                  render={({ field: tipField }) => (
                    <FormField
                      label="Dica ou cuidado"
                      multiline
                      onChangeText={tipField.onChange}
                      value={tipField.value}
                    />
                  )}
                />
              </ItemCard>
            ))}
            <Button
              icon="plus"
              label="Adicionar dica"
              onPress={() => tips.append({ tom: 'info', texto: '' })}
              variant="secondary"
            />
          </AccordionSection>

          <AccordionSection
            expanded={expanded.execution}
            onToggle={() => toggle('execution')}
            title="Execução"
          >
            <Controller
              control={control}
              name="duracaoMinutos"
              render={({ field }) => (
                <FormField
                  error={message(errors.duracaoMinutos)}
                  keyboardType="number-pad"
                  label="Duração em minutos"
                  onBlur={field.onBlur}
                  onChangeText={field.onChange}
                  required
                  value={field.value}
                />
              )}
            />
            <Controller
              control={control}
              name="videoYoutube"
              render={({ field }) => (
                <FormField
                  autoCapitalize="none"
                  autoCorrect={false}
                  error={message(errors.videoYoutube)}
                  keyboardType="url"
                  label="Link do vídeo (YouTube)"
                  onBlur={field.onBlur}
                  onChangeText={field.onChange}
                  required
                  value={field.value}
                />
              )}
            />
            <FilePickerField
              error={message(errors.capa)}
              file={values.capa}
              label="Thumbnail / imagem de capa"
              onPick={() => void chooseImage((file) => setValue('capa', file, { shouldValidate: true }))}
              onRemove={() => setValue('capa', null, { shouldValidate: true })}
              required
            />
            <Controller
              control={control}
              name="capaAlt"
              render={({ field }) => (
                <FormField
                  error={message(errors.capaAlt)}
                  label="Texto alternativo da capa"
                  multiline
                  onBlur={field.onBlur}
                  onChangeText={field.onChange}
                  required
                  value={field.value}
                />
              )}
            />

            {steps.fields.map((field, index) => (
              <ItemCard key={field.id} onRemove={() => steps.remove(index)} title={`Passo ${index + 1}`}>
                <Controller
                  control={control}
                  name={`passos.${index}.titulo`}
                  render={({ field: stepField }) => (
                    <FormField
                      error={message(errors.passos?.[index]?.titulo)}
                      label="Título do passo"
                      onChangeText={stepField.onChange}
                      value={stepField.value}
                    />
                  )}
                />
                <Controller
                  control={control}
                  name={`passos.${index}.corpo`}
                  render={({ field: stepField }) => (
                    <FormField
                      error={message(errors.passos?.[index]?.corpo)}
                      label="Descrição"
                      multiline
                      onChangeText={stepField.onChange}
                      value={stepField.value}
                    />
                  )}
                />
                <FilePickerField
                  file={values.passos[index]?.imagem ?? null}
                  label="Imagem do passo"
                  onPick={() =>
                    void chooseImage((file) => setValue(`passos.${index}.imagem`, file, { shouldValidate: true }))
                  }
                  onRemove={() => setValue(`passos.${index}.imagem`, null)}
                />
              </ItemCard>
            ))}
            <Button
              icon="plus"
              label="Adicionar passo"
              onPress={() => steps.append({ titulo: '', corpo: '', imagem: null })}
              variant="secondary"
            />

            {gallery.fields.map((field, index) => (
              <ItemCard key={field.id} onRemove={() => gallery.remove(index)} title={`Imagem ${index + 1}`}>
                <FilePickerField
                  error={message(errors.galeria?.[index]?.arquivo)}
                  file={values.galeria[index]?.arquivo ?? null}
                  label="Arquivo de imagem"
                  onPick={() =>
                    void chooseImage((file) => setValue(`galeria.${index}.arquivo`, file, { shouldValidate: true }))
                  }
                  onRemove={() => setValue(`galeria.${index}.arquivo`, null)}
                />
                <Controller
                  control={control}
                  name={`galeria.${index}.alt`}
                  render={({ field: galleryField }) => (
                    <FormField
                      error={message(errors.galeria?.[index]?.alt)}
                      label="Texto alternativo"
                      onChangeText={galleryField.onChange}
                      value={galleryField.value}
                    />
                  )}
                />
              </ItemCard>
            ))}
            <Button
              icon="image-plus"
              label="Adicionar imagem à galeria"
              onPress={() => gallery.append({ alt: '', arquivo: null })}
              variant="secondary"
            />
          </AccordionSection>

          <AccordionSection
            expanded={expanded.files}
            onToggle={() => toggle('files')}
            title="Arquivos complementares"
          >
            <Text style={styles.subheading}>Baixáveis (PDF, DOC ou ZIP)</Text>
            {downloads.fields.map((field, index) => (
              <ItemCard key={field.id} onRemove={() => downloads.remove(index)} title={`Baixável ${index + 1}`}>
                <Controller
                  control={control}
                  name={`baixaveis.${index}.rotulo`}
                  render={({ field: downloadField }) => (
                    <FormField
                      error={message(errors.baixaveis?.[index]?.rotulo)}
                      label="Rótulo"
                      onChangeText={downloadField.onChange}
                      value={downloadField.value}
                    />
                  )}
                />
                <ChoiceChips
                  label="Tipo"
                  onChange={(selected) =>
                    setValue(`baixaveis.${index}.tipo`, (selected[0] ?? 'pdf') as 'pdf' | 'doc' | 'zip')
                  }
                  options={taxonomy.data?.tiposBaixaveis ?? []}
                  values={[values.baixaveis[index]?.tipo ?? 'pdf']}
                />
                <FilePickerField
                  error={message(errors.baixaveis?.[index]?.arquivo)}
                  file={values.baixaveis[index]?.arquivo ?? null}
                  label="Arquivo"
                  onPick={() =>
                    void chooseDocument((file) =>
                      setValue(`baixaveis.${index}.arquivo`, file, { shouldValidate: true })
                    )
                  }
                  onRemove={() => setValue(`baixaveis.${index}.arquivo`, null)}
                />
              </ItemCard>
            ))}
            <Button
              icon="file-plus-outline"
              label="Adicionar baixável"
              onPress={() => downloads.append({ rotulo: '', tipo: 'pdf', arquivo: null })}
              variant="secondary"
            />

            <Text style={styles.subheading}>Outros arquivos</Text>
            {files.fields.map((field, index) => (
              <ItemCard key={field.id} onRemove={() => files.remove(index)} title={`Arquivo ${index + 1}`}>
                <Controller
                  control={control}
                  name={`arquivos.${index}.rotulo`}
                  render={({ field: fileField }) => (
                    <FormField
                      error={message(errors.arquivos?.[index]?.rotulo)}
                      label="Rótulo"
                      onChangeText={fileField.onChange}
                      value={fileField.value}
                    />
                  )}
                />
                <ChoiceChips
                  label="Tipo"
                  onChange={(selected) =>
                    setValue(
                      `arquivos.${index}.tipo`,
                      (selected[0] ?? 'other') as PublicationFormValues['arquivos'][number]['tipo']
                    )
                  }
                  options={taxonomy.data?.tiposArquivos ?? []}
                  values={[values.arquivos[index]?.tipo ?? 'other']}
                />
                <FilePickerField
                  error={message(errors.arquivos?.[index]?.arquivo)}
                  file={values.arquivos[index]?.arquivo ?? null}
                  label="Arquivo"
                  onPick={() =>
                    void chooseDocument((file) =>
                      setValue(`arquivos.${index}.arquivo`, file, { shouldValidate: true })
                    )
                  }
                  onRemove={() => setValue(`arquivos.${index}.arquivo`, null)}
                />
              </ItemCard>
            ))}
            <Button
              icon="file-plus-outline"
              label="Adicionar arquivo"
              onPress={() => files.append({ rotulo: '', tipo: 'other', arquivo: null })}
              variant="secondary"
            />
          </AccordionSection>

          <AccordionSection
            expanded={expanded.other}
            onToggle={() => toggle('other')}
            title="Outras informações"
          >
            <DynamicTextList
              error={message(errors.tags as FieldError)}
              label="Palavras-chave / tags"
              onAdd={() => tags.append({ valor: '' })}
              onChange={(index, value) => setValue(`tags.${index}.valor`, value, { shouldValidate: true })}
              onRemove={tags.remove}
              required
              values={tags.fields.map((field, index) => ({ ...field, valor: values.tags[index]?.valor ?? '' }))}
            />
          </AccordionSection>

          {publishing ? (
            <Text accessibilityLiveRegion="polite" style={styles.uploading}>
              Enviando metadados e arquivos em uma única publicação…
            </Text>
          ) : null}
          <Button
            disabled={publishing}
            label="Publicar"
            loading={publishing}
            onPress={() => void submit()}
            testID="publish-button"
          />
          <Button disabled={publishing} label="Cancelar" onPress={cancel} variant="secondary" />
        </AsyncState>
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  content: { paddingHorizontal: spacing.lg, paddingBottom: spacing.xxl, gap: spacing.lg },
  title: { color: colors.text, fontSize: 23, fontWeight: '900', textAlign: 'center', textTransform: 'uppercase' },
  requiredHint: { color: colors.muted, textAlign: 'center', fontSize: 12 },
  itemCard: {
    borderRadius: radii.md,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: 'rgba(255,255,255,0.58)',
    padding: spacing.md,
    gap: spacing.md,
    ...shadows.card
  },
  itemHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  itemTitle: { color: colors.brandDeep, fontSize: 15, fontWeight: '900' },
  subheading: { color: colors.text, fontWeight: '900', fontSize: 15 },
  serverError: {
    flexDirection: 'row',
    gap: spacing.sm,
    alignItems: 'center',
    backgroundColor: colors.dangerSoft,
    borderRadius: radii.md,
    padding: spacing.md
  },
  serverErrorText: { color: colors.danger, flex: 1, fontWeight: '700' },
  uploading: { color: colors.brand, fontWeight: '700', textAlign: 'center' },
  successContent: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: spacing.xl,
    gap: spacing.lg,
    alignItems: 'stretch'
  },
  successTitle: { color: colors.text, textAlign: 'center', fontSize: 25, fontWeight: '900' },
  successSlug: {
    color: colors.brandDeep,
    backgroundColor: colors.panel,
    padding: spacing.md,
    borderRadius: radii.md,
    textAlign: 'center',
    fontWeight: '800'
  },
  successMessage: { color: colors.muted, textAlign: 'center', lineHeight: 21 }
});
