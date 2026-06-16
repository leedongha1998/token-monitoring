export interface Project {
  id: number;
  name: string;
  description: string;
  active: boolean;
  createdAt: string;
}

export interface ApiKey {
  id: number;
  prefix: string;
  active: boolean;
  createdAt: string;
}

export interface IssuedKey {
  id: number;
  prefix: string;
  plainKey: string;
  createdAt: string;
}
