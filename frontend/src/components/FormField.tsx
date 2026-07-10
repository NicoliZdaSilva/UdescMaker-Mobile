import { StyleSheet, Text, TextInput, View, type TextInputProps } from 'react-native';

import { colors, radii, spacing } from '../theme';

interface FormFieldProps extends TextInputProps {
  label: string;
  required?: boolean;
  error?: string;
  counter?: string;
}

export function FormField({ label, required, error, counter, style, ...props }: FormFieldProps) {
  return (
    <View style={styles.group}>
      <View style={styles.labelRow}>
        <Text style={styles.label}>
          {label} {required ? <Text style={styles.required}>*</Text> : null}
        </Text>
        {counter ? <Text style={styles.counter}>{counter}</Text> : null}
      </View>
      <TextInput
        accessibilityLabel={label}
        aria-invalid={Boolean(error)}
        placeholderTextColor="#76827c"
        style={[styles.input, props.multiline && styles.multiline, error && styles.inputError, style]}
        {...props}
      />
      {error ? (
        <Text accessibilityLiveRegion="polite" style={styles.error}>
          {error}
        </Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  group: { gap: 6 },
  labelRow: { flexDirection: 'row', justifyContent: 'space-between', gap: spacing.sm },
  label: { color: colors.text, fontSize: 14, fontWeight: '700', flexShrink: 1 },
  required: { color: colors.danger },
  counter: { color: colors.muted, fontSize: 12 },
  input: {
    minHeight: 50,
    borderRadius: radii.md,
    borderWidth: 1,
    borderColor: colors.line,
    backgroundColor: colors.white,
    color: colors.text,
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
    fontSize: 15
  },
  multiline: { minHeight: 92, textAlignVertical: 'top' },
  inputError: { borderColor: colors.danger, borderWidth: 1.5 },
  error: { color: colors.danger, fontSize: 12, fontWeight: '600' }
});
