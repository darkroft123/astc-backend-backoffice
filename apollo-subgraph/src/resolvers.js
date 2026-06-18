import { callBackoffice } from './client.js';

export const resolvers = {
  Query: {
    async ListUsers(_, __, contextValue) {
      const data = await callBackoffice(
        `query {
          ListUsers {
            id
            username
            firstName
            lastName
            email
            phone
            roleId
            roleCode
            roleName
          }
        }`,
        {},
        contextValue.token
      );

      return data.ListUsers;
    },

    async GetUser(_, { id }, contextValue) {
      const data = await callBackoffice(
        `query($id: String!) {
          GetUser(id: $id) {
            id
            username
            firstName
            lastName
            email
            phone
            roleId
            roleCode
            roleName
          }
        }`,
        { id },
        contextValue.token
      );

      return data.GetUser;
    }
  },

  Mutation: {
    async createUser(_, { input }, contextValue) {
      const data = await callBackoffice(
        `mutation($input: CreateUserInput!) {
          createUser(input: $input) {
            id
            username
            firstName
            lastName
            email
            phone
            roleId
            roleCode
            roleName
          }
        }`,
        { input },
        contextValue.token
      );

      return data.createUser;
    },

    async editUser(_, { id, input }, contextValue) {
      const data = await callBackoffice(
        `mutation($id: String!, $input: EditUserInput!) {
          editUser(id: $id, input: $input) {
            id
            username
            firstName
            lastName
            email
            phone
            roleId
            roleCode
            roleName
          }
        }`,
        { id, input },
        contextValue.token
      );

      return data.editUser;
    },

    async deleteUser(_, { id }, contextValue) {
      const data = await callBackoffice(
        `mutation($id: String!) {
          deleteUser(id: $id)
        }`,
        { id },
        contextValue.token
      );

      return data.deleteUser;
    }
  }
};