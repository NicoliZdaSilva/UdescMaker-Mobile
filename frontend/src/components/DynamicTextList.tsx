import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, TextInput, View } from 'react-native';

import { colors, radii, spacing } from '../theme';
import { Button } from './Button';

interface DynamicTextListProps {
  label: string;
  values: Array<{ id?: string; valor: string }>;
  onChange: (index: number, value: string) => void;
  onAdd: () => void;
  onRemove: (index: number) => void;
  required?: boolean;
  error?: string;
  addLabel?: string;
}

export function DynamicTextList({
  label,
  values,
  onChange,
  onAdd,
  onRemove,
  required,
  error,
  addLabel = 'Adicionar item'
}: DynamicTextListProps) {
  return (
    <View style={styles.group}>
      <Text style={styles.label}>
        {label} {required ? <Text style={styles.required}>*</Text> : null}
      </Text>
      {values.map((item, index) => (
        <View key={item.id ?? index} style={styles.row}>
          <TextInput
            accessibilityLabel={`${label} ${index + 1}`}
            onChangeText={(value) => onChange(index, value)}
            placeholder={`Item ${index + 1}`}
            placeholderTextColor="#76827c"
            style={styles.input}
            value={item.valor}
          />
          <Pressable
            accessibilityLabel={`Remover item ${index + 1} de ${label}`}
            accessibilityRole="button"
            hitSlop={8}
            onPress={() => onRemove(index)}
            style={({ pressed }) => pressed && styles.pressed}
          >
            <MaterialCommunityIcons color={colors.danger} name="delete-outline" size={24} />
          </Pressable>
        </View>
      ))}
      <Button icon="plus" label={addLabel} onPress={onAdd} variant="secondary" />
      {error ? <Text style={styles.error}>{error}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  group: { gap: spacing.sm },
  label: { color: colors.text, fontWeight: '700', fontSize: 14 },
  required: { color: colors.danger },
  row: { flexDirection: 'row', alignItems: 'center', gap: spacing.sm },
  input: {
    flex: 1,
    minHeight: 48,
    borderRadius: radii.md,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.white,
    color: colors.text,
    paddingHorizontal: spacing.lg
  },
  pressed: { opacity: 0.55 },
  error: { color: colors.danger, fontSize: 12, fontWeight: '600' }
});
