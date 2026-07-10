import type { PropsWithChildren } from 'react';
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';
import { MaterialCommunityIcons } from '@expo/vector-icons';

import { colors, spacing } from '../theme';
import { Button } from './Button';

interface AsyncStateProps extends PropsWithChildren {
  loading: boolean;
  error?: Error | null;
  empty?: boolean;
  emptyTitle?: string;
  emptyMessage?: string;
  onRetry?: () => void;
  onEmptyAction?: () => void;
  emptyActionLabel?: string;
}

export function AsyncState({
  loading,
  error,
  empty,
  children,
  emptyTitle = 'Nenhum projeto encontrado',
  emptyMessage = 'Tente alterar ou limpar os filtros.',
  onRetry,
  onEmptyAction,
  emptyActionLabel = 'Alterar filtros'
}: AsyncStateProps) {
  if (loading) {
    return (
      <View accessibilityLabel="Carregando projetos" style={styles.state} testID="loading-state">
        <ActivityIndicator color={colors.brand} size="large" />
        <Text style={styles.message}>Carregando projetos…</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View accessibilityLiveRegion="polite" style={styles.state} testID="error-state">
        <MaterialCommunityIcons color={colors.danger} name="wifi-alert" size={46} />
        <Text style={styles.title}>Não foi possível carregar</Text>
        <Text style={styles.message}>{error.message}</Text>
        {onRetry ? <Button label="Tentar novamente" onPress={onRetry} /> : null}
      </View>
    );
  }

  if (empty) {
    return (
      <View accessibilityLiveRegion="polite" style={styles.state} testID="empty-state">
        <MaterialCommunityIcons color={colors.brand} name="magnify-close" size={46} />
        <Text style={styles.title}>{emptyTitle}</Text>
        <Text style={styles.message}>{emptyMessage}</Text>
        {onEmptyAction ? <Button label={emptyActionLabel} onPress={onEmptyAction} /> : null}
      </View>
    );
  }

  return <>{children}</>;
}

const styles = StyleSheet.create({
  state: {
    minHeight: 230,
    padding: spacing.xl,
    alignItems: 'center',
    justifyContent: 'center',
    gap: spacing.md
  },
  title: { color: colors.text, fontWeight: '900', fontSize: 20, textAlign: 'center' },
  message: { color: colors.muted, textAlign: 'center', lineHeight: 21 }
});
