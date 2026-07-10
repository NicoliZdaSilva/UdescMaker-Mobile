import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, radii, spacing } from '../theme';
import type { SelectedFile } from '../types/forms';

interface FilePickerFieldProps {
  label: string;
  file: SelectedFile | null;
  onPick: () => void;
  onRemove: () => void;
  required?: boolean;
  error?: string;
}

export function FilePickerField({
  label,
  file,
  onPick,
  onRemove,
  required,
  error
}: FilePickerFieldProps) {
  return (
    <View style={styles.group}>
      <Text style={styles.label}>
        {label} {required ? <Text style={styles.required}>*</Text> : null}
      </Text>
      <View style={[styles.shell, error && styles.errorBorder]}>
        <MaterialCommunityIcons
          color={file ? colors.brand : colors.muted}
          name={file ? 'file-check-outline' : 'file-plus-outline'}
          size={24}
        />
        <View style={styles.info}>
          <Text numberOfLines={1} style={styles.fileName}>
            {file?.name ?? 'Nenhum arquivo selecionado'}
          </Text>
          {file?.size ? <Text style={styles.size}>{Math.ceil(file.size / 1024)} KB</Text> : null}
        </View>
        <Pressable accessibilityRole="button" onPress={onPick} style={styles.action}>
          <Text style={styles.actionText}>{file ? 'Substituir' : 'Selecionar'}</Text>
        </Pressable>
        {file ? (
          <Pressable accessibilityLabel={`Remover ${file.name}`} hitSlop={8} onPress={onRemove}>
            <MaterialCommunityIcons color={colors.danger} name="close-circle" size={23} />
          </Pressable>
        ) : null}
      </View>
      {error ? <Text style={styles.error}>{error}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  group: { gap: 6 },
  label: { color: colors.text, fontSize: 14, fontWeight: '700' },
  required: { color: colors.danger },
  shell: {
    minHeight: 60,
    backgroundColor: colors.white,
    borderRadius: radii.md,
    borderWidth: 1,
    borderColor: colors.line,
    paddingHorizontal: spacing.md,
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm
  },
  errorBorder: { borderColor: colors.danger, borderWidth: 1.5 },
  info: { flex: 1, minWidth: 0 },
  fileName: { color: colors.text, fontWeight: '700', fontSize: 13 },
  size: { color: colors.muted, fontSize: 11 },
  action: { paddingVertical: spacing.sm, paddingHorizontal: spacing.xs },
  actionText: { color: colors.brand, fontWeight: '800', fontSize: 12 },
  error: { color: colors.danger, fontSize: 12, fontWeight: '600' }
});
