import { Pressable, StyleSheet, Text, View } from 'react-native';

import { colors, radii, spacing } from '../theme';
import type { TaxonomyOption } from '../types/api';

interface ChoiceChipsProps {
  label: string;
  options: TaxonomyOption[];
  values: string[];
  onChange: (values: string[]) => void;
  multiple?: boolean;
  error?: string;
  required?: boolean;
}

export function ChoiceChips({
  label,
  options,
  values,
  onChange,
  multiple = false,
  error,
  required
}: ChoiceChipsProps) {
  function toggle(id: string) {
    if (!multiple) return onChange(values.includes(id) ? [] : [id]);
    onChange(values.includes(id) ? values.filter((value) => value !== id) : [...values, id]);
  }

  return (
    <View style={styles.group}>
      <Text style={styles.label}>
        {label} {required ? <Text style={styles.required}>*</Text> : null}
      </Text>
      <View accessibilityRole="radiogroup" style={styles.row}>
        {options.map((option) => {
          const selected = values.includes(option.id);
          return (
            <Pressable
              accessibilityRole={multiple ? 'checkbox' : 'radio'}
              accessibilityState={{ checked: selected }}
              key={option.id}
              onPress={() => toggle(option.id)}
              style={({ pressed }) => [
                styles.chip,
                selected && styles.selected,
                pressed && styles.pressed
              ]}
            >
              <Text style={[styles.chipText, selected && styles.selectedText]}>{option.label}</Text>
            </Pressable>
          );
        })}
      </View>
      {error ? <Text style={styles.error}>{error}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  group: { gap: spacing.sm },
  label: { color: colors.text, fontWeight: '700', fontSize: 14 },
  required: { color: colors.danger },
  row: { flexDirection: 'row', flexWrap: 'wrap', gap: spacing.sm },
  chip: {
    minHeight: 38,
    justifyContent: 'center',
    paddingHorizontal: spacing.md,
    borderRadius: radii.pill,
    backgroundColor: colors.white,
    borderWidth: 1,
    borderColor: colors.line
  },
  selected: { backgroundColor: colors.brand, borderColor: colors.brand },
  chipText: { color: colors.text, fontSize: 13, fontWeight: '600' },
  selectedText: { color: colors.white },
  pressed: { opacity: 0.68 },
  error: { color: colors.danger, fontSize: 12, fontWeight: '600' }
});
