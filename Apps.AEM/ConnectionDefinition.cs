using System;
using Blackbird.Applications.Sdk.Common.Authentication;
using Blackbird.Applications.Sdk.Common.Connections;

namespace Apps.AEM
{
	public class ConnectionDefinition : IConnectionDefinition
    {
        public IEnumerable<ConnectionPropertyGroup> ConnectionPropertyGroups => new List<ConnectionPropertyGroup>()
        {
            new ConnectionPropertyGroup
            {
                Name = "js library auth",
                AuthenticationType = ConnectionAuthenticationType.OAuth2,
                ConnectionUsage = ConnectionUsage.Actions,
                ConnectionProperties = new List<ConnectionProperty>()
                {
                    new ConnectionProperty("domainName"),
                    new ConnectionProperty("credentialsJSON"),
                }
            }

        };

        public IEnumerable<AuthenticationCredentialsProvider> CreateAuthorizationCredentialsProviders(Dictionary<string, string> values)
        {
            string domainName;
            string credentialsJsonString;

            try
            {
                domainName = values.First(v => v.Key == "domainName").Value;
            }
            catch
            {
                throw new Exception("domain name not found");
            }
            yield return new AuthenticationCredentialsProvider(
                AuthenticationCredentialsRequestLocation.QueryString,
                "domainName",
                domainName
             );

            try
            {
                credentialsJsonString = values.First(v => v.Key == "credentialsJSON").Value;
            }
            catch
            {
                throw new Exception("credentials json not found");
            }

            yield return new AuthenticationCredentialsProvider(
                AuthenticationCredentialsRequestLocation.QueryString,
                "credentialsJSON",
                credentialsJsonString
             );

        }
    }
}

