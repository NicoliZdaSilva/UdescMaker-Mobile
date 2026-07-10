import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Image } from 'expo-image';
import { Alert, Pressable, RefreshControl, ScrollView, StyleSheet, Text, View } from 'react-native';

import { AsyncState } from '../components/AsyncState';
import { BrandHeader } from '../components/BrandHeader';
import { ContentSection } from '../components/ContentSection';
import { MarkdownText } from '../components/MarkdownText';
import { RelatedProjects } from '../components/RelatedProjects';
import { Screen } from '../components/Screen';
import { YouTubeEmbed } from '../components/YouTubeEmbed';
import { useProject } from '../hooks/useProjects';
import type { ScreenProps } from '../navigation/types';
import { colors, radii, shadows, spacing } from '../theme';
import type { ProjectFile } from '../types/api';
import { difficultyLabel, formatDate, formatDuration } from '../utils/format';
import { openSafePublicUrl } from '../utils/links';

function BulletList({ items }: { items: string[] }) {
  if (items.length === 0) return <Text style={styles.muted}>Não informado.</Text>;
  return (
    <View style={styles.list}>
      {items.map((item, index) => (
        <View key={`${item}-${index}`} style={styles.bulletRow}>
          <Text style={styles.bullet}>•</Text>
          <Text style={styles.bodyText}>{item}</Text>
        </View>
      ))}
    </View>
  );
}

function FileLinks({ files }: { files: ProjectFile[] }) {
  async function open(file: ProjectFile) {
    try {
      await openSafePublicUrl(file.arquivo);
    } catch (error) {
      Alert.alert('Arquivo indisponível', error instanceof Error ? error.message : 'Não foi possível abrir.');
    }
  }

  return (
    <View style={styles.fileList}>
      {files.map((file, index) => (
        <Pressable
          accessibilityHint="Abre o arquivo em outro aplicativo"
          accessibilityRole="link"
          key={`${file.arquivo}-${index}`}
          onPress={() => void open(file)}
          style={({ pressed }) => [styles.file, pressed && styles.pressed]}
        >
          <MaterialCommunityIcons color={colors.brand} name="file-download-outline" size={24} />
          <View style={styles.fileInfo}>
            <Text style={styles.fileTitle}>{file.rotulo}</Text>
            <Text style={styles.fileType}>{file.tipo.toUpperCase()}</Text>
          </View>
        </Pressable>
      ))}
    </View>
  );
}

export function DetailsScreen({ navigation, route }: ScreenProps<'Details'>) {
  const project = useProject(route.params.slug);

  return (
    <Screen>
      <BrandHeader compact onBack={navigation.goBack} />
      <ScrollView
        contentContainerStyle={styles.content}
        refreshControl={<RefreshControl onRefresh={project.reload} refreshing={project.refreshing} />}
      >
        <AsyncState error={project.error} loading={project.loading} onRetry={project.reload}>
          {project.data ? (
            <>
              <Text accessibilityRole="header" style={styles.title}>
                {project.data.titulo}
              </Text>
              <Text style={styles.lede}>{project.data.resumo}</Text>
              <Text style={styles.author}>Por {project.data.autor.nome}</Text>

              <YouTubeEmbed title={project.data.titulo} url={project.data.videoYoutube} />

              {!project.data.videoYoutube && project.data.capa.src ? (
                <Image
                  accessibilityLabel={project.data.capa.alt}
                  contentFit="cover"
                  source={project.data.capa.src}
                  style={styles.cover}
                />
              ) : null}

              <View style={styles.chips}>
                {project.data.categorias.map((category) => (
                  <Text key={category} style={styles.categoryChip}>
                    {category}
                  </Text>
                ))}
                {project.data.tags.map((tag) => (
                  <Text key={tag} style={styles.tagChip}>
                    #{tag}
                  </Text>
                ))}
              </View>

              <View style={styles.metadata}>
                <Text style={styles.meta}>📅 {formatDate(project.data.publicadoEm)}</Text>
                <Text style={styles.meta}>◷ {formatDuration(project.data.duracaoMinutos)}</Text>
                <Text style={styles.meta}>◎ {project.data.idadeMinima}+ anos</Text>
                <Text style={styles.meta}>✦ {difficultyLabel(project.data.dificuldade)}</Text>
              </View>

              {project.data.corpoMarkdown ? (
                <ContentSection title="Sobre o projeto">
                  <MarkdownText markdown={project.data.corpoMarkdown} />
                </ContentSection>
              ) : null}

              <ContentSection title="Materiais">
                <BulletList items={project.data.materiais} />
              </ContentSection>
              <ContentSection title="Ferramentas">
                <BulletList items={project.data.ferramentas} />
              </ContentSection>

              {project.data.passos.length > 0 ? (
                <ContentSection title="Passo a passo">
                  <View style={styles.stepList}>
                    {project.data.passos.map((step, index) => (
                      <View key={`${step.titulo}-${index}`} style={styles.stepCard}>
                        <Text style={styles.stepIndex}>{String(index + 1).padStart(2, '0')}</Text>
                        <View style={styles.stepBody}>
                          <Text style={styles.stepTitle}>{step.titulo}</Text>
                          <Text style={styles.bodyText}>{step.corpo}</Text>
                          {step.imagem ? (
                            <Image
                              accessibilityLabel={`Imagem do passo ${index + 1}: ${step.titulo}`}
                              contentFit="cover"
                              source={step.imagem}
                              style={styles.stepImage}
                            />
                          ) : null}
                        </View>
                      </View>
                    ))}
                  </View>
                </ContentSection>
              ) : null}

              {project.data.dicas.length > 0 ? (
                <ContentSection title="Dicas e cuidados">
                  {project.data.dicas.map((tip, index) => (
                    <View key={`${tip.texto}-${index}`} style={[styles.tip, styles[`tip_${tip.tom}`]]}>
                      <Text style={styles.bodyText}>{tip.texto}</Text>
                    </View>
                  ))}
                </ContentSection>
              ) : null}

              {project.data.galeria.length > 0 ? (
                <ContentSection title="Galeria">
                  <ScrollView horizontal showsHorizontalScrollIndicator={false}>
                    <View style={styles.gallery}>
                      {project.data.galeria.map((image, index) => (
                        <Image
                          accessibilityLabel={image.alt}
                          contentFit="cover"
                          key={`${image.src}-${index}`}
                          source={image.src}
                          style={styles.galleryImage}
                        />
                      ))}
                    </View>
                  </ScrollView>
                </ContentSection>
              ) : null}

              {project.data.baixaveis.length + project.data.arquivos.length > 0 ? (
                <ContentSection title="Arquivos complementares">
                  <FileLinks files={[...project.data.baixaveis, ...project.data.arquivos]} />
                </ContentSection>
              ) : null}

              <RelatedProjects
                onOpen={(slug) => navigation.push('Details', { slug })}
                projects={project.data.relacionados}
              />
            </>
          ) : null}
        </AsyncState>
      </ScrollView>
    </Screen>
  );
}

const styles = StyleSheet.create({
  content: { padding: spacing.lg, paddingTop: spacing.sm, paddingBottom: spacing.xxl, gap: spacing.lg },
  title: { color: colors.text, fontSize: 27, lineHeight: 33, fontWeight: '900', textTransform: 'uppercase' },
  lede: { color: colors.muted, fontSize: 16, lineHeight: 24 },
  author: { color: colors.brandDeep, fontSize: 13, fontWeight: '800' },
  cover: { width: '100%', height: 220, borderRadius: radii.lg, backgroundColor: colors.white },
  chips: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  categoryChip: {
    backgroundColor: colors.brand,
    color: colors.white,
    paddingHorizontal: spacing.md,
    paddingVertical: 5,
    borderRadius: radii.pill,
    overflow: 'hidden',
    textTransform: 'capitalize',
    fontSize: 12
  },
  tagChip: {
    backgroundColor: '#d9c8a3',
    color: colors.text,
    paddingHorizontal: spacing.md,
    paddingVertical: 5,
    borderRadius: radii.pill,
    overflow: 'hidden',
    fontSize: 12
  },
  metadata: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.md },
  meta: { color: colors.muted, fontWeight: '700', fontSize: 13 },
  muted: { color: colors.muted, fontStyle: 'italic' },
  list: { gap: spacing.sm },
  bulletRow: { flexDirection: 'row', gap: spacing.sm },
  bullet: { color: colors.brand, fontWeight: '900' },
  bodyText: { color: colors.muted, lineHeight: 22, flexShrink: 1 },
  stepList: { gap: spacing.md },
  stepCard: { flexDirection: 'row', gap: spacing.md },
  stepIndex: { color: colors.brand, fontSize: 22, fontWeight: '900' },
  stepBody: { flex: 1, gap: spacing.sm },
  stepTitle: { color: colors.text, fontWeight: '800', fontSize: 16 },
  stepImage: { width: '100%', height: 180, borderRadius: radii.md, backgroundColor: colors.white },
  tip: { padding: spacing.md, borderRadius: radii.md, borderLeftWidth: 4 },
  tip_info: { backgroundColor: colors.infoSoft, borderLeftColor: '#367ea8' },
  tip_warning: { backgroundColor: '#fff3d8', borderLeftColor: colors.warning },
  tip_success: { backgroundColor: colors.successSoft, borderLeftColor: colors.success },
  gallery: { flexDirection: 'row', gap: spacing.md },
  galleryImage: { width: 250, height: 170, borderRadius: radii.md, backgroundColor: colors.white },
  fileList: { gap: spacing.sm },
  file: {
    minHeight: 58,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
    backgroundColor: colors.panel,
    borderRadius: radii.md,
    padding: spacing.md,
    ...shadows.card
  },
  fileInfo: { flex: 1 },
  fileTitle: { color: colors.text, fontWeight: '800' },
  fileType: { color: colors.muted, fontSize: 11 },
  pressed: { opacity: 0.62 }
});
