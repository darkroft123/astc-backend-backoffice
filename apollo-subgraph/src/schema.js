import { parse } from 'graphql';

export const typeDefs = parse(/* GraphQL */ `
  extend schema
    @link(url: "https://specs.apollo.dev/federation/v2.3", import: ["@key"])

  type Query {
    ListUsers(filter: ListUsersFilterInput): ListUsersOutput
    GetUser(id: ID!): UserOutput
  }

  type Mutation {
    createUser(input: CreateUserInput!): UserOutput
    editUser(id: ID!, input: EditUserInput!): UserOutput
    deleteUser(id: ID!): Boolean!
  }

  # =========================
  # USER ENTITY (FEDERATION)
  # =========================
  type UserOutput @key(fields: "id") {
    id: ID!
    username: String
    email: String
    firstName: String
    lastName: String
    phone: String
    roleId: String
    avatarUrl: String
    roleCode: String
    roleName: String
  }

  # =========================
  # LIST OUTPUT
  # =========================
  type ListUsersOutput {
    data: [UserOutput]
    pageInfo: PageInfoOutput
  }

  type PageInfoOutput {
    page: Int
    size: Int
    totalElements: Int
    totalPages: Int
  }

  # =========================
  # INPUTS
  # =========================
  input CreateUserInput {
    username: String!
    email: String!
    password: String!
    firstName: String
    lastName: String
    phone: String
    roleId: String!
    avatarUrl: String
  }

  input EditUserInput {
    username: String
    email: String
    firstName: String
    lastName: String
    phone: String
    roleId: String
    avatarUrl: String
  }

  input DeleteUserInput {
    id: ID!
  }

  input ListUsersFilterInput {
    username: String
    email: String
    roleId: String
    page: Int
    size: Int
  }
`);