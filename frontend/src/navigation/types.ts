import type { NativeStackScreenProps } from '@react-navigation/native-stack';

import type { ProjectFilters } from '../types/api';

export type RootStackParamList = {
  Home: undefined;
  Publish: undefined;
  Details: { slug: string };
  Explore: { initialFilters?: ProjectFilters } | undefined;
  Results: { filters: ProjectFilters };
};

export type ScreenProps<T extends keyof RootStackParamList> = NativeStackScreenProps<
  RootStackParamList,
  T
>;
