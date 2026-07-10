import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Pressable, StyleSheet, TextInput, View } from 'react-native';

import { colors, radii, shadows, spacing } from '../theme';

interface SearchBarProps {
  value: string;
  onChangeText: (value: string) => void;
  onSubmit: () => void;
  placeholder?: string;
}

export function SearchBar({ value, onChangeText, onSubmit, placeholder }: SearchBarProps) {
  return (
    <View style={[styles.shell, shadows.card]}>
      <TextInput
        accessibilityLabel="Buscar projetos"
        onChangeText={onChangeText}
        onSubmitEditing={onSubmit}
        placeholder={placeholder ?? 'Busque projetos maker'}
        placeholderTextColor="#758079"
        returnKeyType="search"
        style={styles.input}
        value={value}
      />
      <Pressable accessibilityLabel="Executar busca" hitSlop={10} onPress={onSubmit}>
        <MaterialCommunityIcons color={colors.brandDeep} name="magnify" size={24} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  shell: {
    minHeight: 50,
    backgroundColor: colors.white,
    borderRadius: radii.pill,
    borderWidth: 1,
    borderColor: colors.line,
    paddingHorizontal: spacing.lg,
    flexDirection: 'row',
    alignItems: 'center'
  },
  input: { flex: 1, color: colors.text, fontSize: 15, paddingVertical: spacing.md }
});
